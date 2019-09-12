package ru.roborox.crawler.proxy

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import ru.roborox.crawler.http.BlockingQueueReactiveResources
import ru.roborox.crawler.http.WebClientAndProxy
import java.util.concurrent.TimeUnit

object ProxyWebClients {

    private const val TIMEOUT = 10000

    fun createClients(proxies: Collection<Proxy>, cookiesConsumer: (MultiValueMap<String, String>) -> Unit = {}): BlockingQueueReactiveResources<WebClientAndProxy> {
        return BlockingQueueReactiveResources(proxies.map { WebClientAndProxy(createClient(it, cookiesConsumer), it) })
    }

    private fun createClient(proxy: Proxy, cookiesConsumer: (MultiValueMap<String, String>) -> Unit): WebClient {
        val httpClient = HttpClient.create()
            .tcpConfiguration { client ->
                client
                    .proxy { it.type(proxy.type).host(proxy.host).port(proxy.port) }
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                    .doOnConnected {
                        it.addHandlerLast(ReadTimeoutHandler(TIMEOUT.toLong(), TimeUnit.MILLISECONDS))
                    }
            }
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultCookies { cookiesConsumer(it) }
            .defaultHeaders {
                it.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36")
                it.add(HttpHeaders.ACCEPT, "*/*")
                it.add(HttpHeaders.CACHE_CONTROL, "no-cache")
                it.add(HttpHeaders.CONNECTION, "keep-alive")
            }
            .build()
    }
}
