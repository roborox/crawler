package ru.roborox.crawler.http

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
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
import reactor.core.publisher.toMono
import ru.roborox.crawler.LoadResult
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.kotlin.toMuliValueMap
import java.text.SimpleDateFormat
import java.util.*

interface HttpClient {
    fun get(url: String, cookie: MultiValueMap<String, String> = LinkedMultiValueMap()): Mono<HttpResponse<String>>
    fun getBytes(url: String): Flux<DataBuffer>
}

//todo save load logs
@Component
class HttpClientImpl : HttpClient {
    private val client = WebClient.builder()
        .clientConnector(WebClientHelper.createConnector(10000, 10000))
        .defaultHeaders {
            it.add(HttpHeaders.USER_AGENT,"PostmanRuntime/7.15.2")
            it.add(HttpHeaders.ACCEPT, "*/*")
            it.add(HttpHeaders.CACHE_CONTROL, "no-cache")
            it.add(HttpHeaders.CONNECTION, "keep-alive")
        }
        .build()

    override fun get(url: String, cookie: MultiValueMap<String, String>): Mono<HttpResponse<String>> {
        return client.get()
            .uri(url)
            .cookies { cookies -> cookies.addAll(cookie) }
            .exchange()
            .map { resp ->
                HttpResponse(resp.headers()) {
                    resp.bodyToMono<String>()
                }
            }
    }

    override fun getBytes(url: String): Flux<DataBuffer> {
        return client.get()
            .uri(url)
            .accept(APPLICATION_PDF)
            .exchange()
            .flatMapMany {
                it.bodyToFlux<DataBuffer>()
            }
    }
}

class HttpResponse<T>(
    val headers: ClientResponse.Headers,
    val body: () -> Mono<T>
)

fun <T> Mono<HttpResponse<T>>.ifChanged(page: Page, loader: (T) -> Mono<LoadResult>): Mono<LoadResult> {
    return this.flatMap {
        if (page.lastLoadDate == null || page.tryLoad(it.headers.parseLastModifiedDate())) {
            it.body()
                .flatMap { body -> loader(body) }
        } else {
            LoadResult.SkippedResult.toMono()
        }
    }
}

private fun Page.tryLoad(lastModifiedDate: Date?): Boolean {
    return lastLoadDate == null || lastModifiedDate == null || lastLoadDate!! < lastModifiedDate
}

private fun ClientResponse.Headers.parseLastModifiedDate(): Date? {
    val headers = header("Last-Modified")
    return if (headers.isEmpty()) {
        null
    } else {
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
        format.parse(headers.first())
    }
}