package ru.roborox.crawler.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.publisher.toMono
import ru.roborox.crawler.LoadResult
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.Raw
import ru.roborox.crawler.persist.RawRepository
import ru.roborox.logging.reactive.LoggingUtils
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

interface HttpClient {
    fun get(url: String, cookie: MultiValueMap<String, String> = LinkedMultiValueMap(), clients: WebClients = DefaultWebClients, handleRedirect: Boolean = false, rawUse: RawUse = RawUse.SAVE): Mono<HttpResponse<String?>>
    fun getBytes(url: String, cookie: MultiValueMap<String, String> = LinkedMultiValueMap(), clients: WebClients = DefaultWebClients): Mono<HttpResponse<Flux<DataBuffer>>>
}

enum class RawUse {
    IGNORE,
    USE_CACHED,
    SAVE
}

private val client = WebClient.builder()
    .clientConnector(WebClientHelper.createConnector(10000, 10000))
    .defaultHeaders {
        it.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36")
        it.add(HttpHeaders.ACCEPT, "*/*")
        it.add(HttpHeaders.CACHE_CONTROL, "no-cache")
        it.add(HttpHeaders.CONNECTION, "keep-alive")
    }
    .build()
private val clientAndProxy = WebClientAndProxy(client)

object DefaultWebClients : WebClients {
    override fun <T> useResource(f: (WebClientAndProxy) -> Mono<T>): Mono<T> = f(clientAndProxy)
}

@Component
class HttpClientImpl(private val rawRepository: RawRepository) : HttpClient {
    override fun get(url: String, cookie: MultiValueMap<String, String>, clients: WebClients, handleRedirect: Boolean, rawUse: RawUse): Mono<HttpResponse<String?>> {
        return if (rawUse == RawUse.USE_CACHED) {
            rawRepository.findById(url).map {
                logger.info("get task using raw")
                HttpResponse<String?>(HttpStatus.OK, null, it.content)
            }.switchIfEmpty {
                logger.info("get task failed. using http")
                getWithUrl(clients, url, cookie, handleRedirect, rawUse)
            }
        } else {
            getWithUrl(clients, url, cookie, handleRedirect, rawUse)
        }
    }

    private fun getWithUrl(clients: WebClients, url: String, cookie: MultiValueMap<String, String>, handleRedirect: Boolean, rawUse: RawUse): Mono<HttpResponse<String?>> {
        return LoggingUtils.withMarker { marker ->
            clients.useResource { clientAndProxy ->
                logger.info(marker, "get $url using ${clientAndProxy.proxy}")
                clientAndProxy.client.get()
                    .uri(url)
                    .cookies { cookies -> cookies.addAll(cookie) }
                    .exchange()
                    .flatMap { resp ->
                        resp.bodyToMono<String>()
                            .toOptional()
                            .map {
                                HttpResponse<String?>(resp.statusCode(), resp.headers(), it.orElse(null))
                            }
                    }
                    .flatMap { resp ->
                        if (handleRedirect && resp.status.is3xxRedirection) {
                            val newUrl = resp.headers?.getLocation(url)
                            logger.info(marker, "handling redirect from $url to $newUrl")
                            clientAndProxy.client.get()
                                .uri(newUrl!!)
                                .cookies { cookies -> cookies.addAll(cookie) }
                                .exchange()
                                .flatMap { secondResp ->
                                    secondResp.bodyToMono<String>()
                                        .toOptional()
                                        .map {
                                            HttpResponse<String?>(secondResp.statusCode(), secondResp.headers(), it.orElse(null))
                                        }
                                }
                        } else {
                            resp.toMono()
                        }
                    }.flatMap {
                        if (rawUse != RawUse.IGNORE) {
                            if (it.body != null)
                                rawRepository.save(Raw(id = url, content = it.body))
                                    .thenReturn(it)
                            else
                                it.toMono()
                        } else {
                            it.toMono()
                        }
                    }
            }
        }
    }

    override fun getBytes(url: String, cookie: MultiValueMap<String, String>, clients: WebClients): Mono<HttpResponse<Flux<DataBuffer>>> {
        return LoggingUtils.withMarker { marker ->
            clients.useResource { clientAndProxy ->
                logger.info(marker, "get bytes $url using ${clientAndProxy.proxy} client:#${clientAndProxy.client.hashCode()}")
                clientAndProxy.client.get()
                    .uri(url)
                    .accept(APPLICATION_PDF)
                    .exchange()
                    .map { resp ->
                        HttpResponse(resp.statusCode(), resp.headers(), resp.bodyToFlux<DataBuffer>())
                    }
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HttpClientImpl::class.java)
    }
}

data class HttpResponse<T>(
    val status: HttpStatus,
    val headers: ClientResponse.Headers?,
    val body: T
) {
    override fun toString(): String {
        return "HttpResponse(status=$status, headers=$headers)"
    }

    val contentType: String?
        get() = headers?.header(HttpHeaders.CONTENT_TYPE)?.first()
}

fun <T> Mono<HttpResponse<T>>.ifChanged(page: Page, loader: (T?) -> Mono<LoadResult>): Mono<LoadResult> {
    return LoggingUtils.withMarker { marker ->
        this.flatMap {
            if (page.lastLoadDate == null || page.tryLoad(it.headers?.parseLastModifiedDate())) {
                Http.logger.info(marker, "page ${page.taskId} changed. continuing")
                loader(it.body)
            } else {
                Http.logger.info(marker, "page ${page.taskId} not changed. ignoring")
                LoadResult.SkippedResult.toMono()
            }
        }
    }
}

object Http {
    val logger: Logger = LoggerFactory.getLogger(Http::class.java)
}

private fun Page.tryLoad(lastModifiedDate: Date?): Boolean {
    return lastLoadDate == null || lastModifiedDate == null || lastLoadDate!! < lastModifiedDate
}

private fun ClientResponse.Headers.parseLastModifiedDate(): Date? {
    val headers = header("Last-Modified")
    return if (headers.isEmpty()) {
        null
    } else {
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        format.parse(headers.first())
    }
}

fun ClientResponse.Headers.getLocation(original: String): String? {
    return this.header("Location").firstOrNull()
        ?.let {
            if (it.startsWith("http")) {
                it
            } else {
                URL(URL(original), it).toString()
            }
        }
}

fun <T> Mono<T>.toOptional(): Mono<Optional<T>> {
    return this
        .map { Optional.of(it) }
        .switchIfEmpty { Optional.empty<T>().toMono() }
}