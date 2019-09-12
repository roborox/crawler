package ru.roborox.crawler.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.roborox.crawler.proxy.Proxy
import ru.roborox.crawler.scheduler.blockingToMono
import ru.roborox.logging.reactive.LoggingUtils
import java.util.concurrent.Callable
import java.util.concurrent.LinkedBlockingQueue

interface ReactiveResources<R> {
    fun <T> useResource(f: (R) -> Mono<T>): Mono<T>
}

typealias WebClients = ReactiveResources<WebClientAndProxy>

data class WebClientAndProxy(val client: WebClient, val proxy: Proxy? = null) {
    override fun toString(): String {
        return "client: #${client.hashCode()}${proxy?.let { " $it" } ?: ""}"
    }
}

class BlockingQueueReactiveResources<R>(resources: Collection<R>) : ReactiveResources<R> {
    private val queue = LinkedBlockingQueue(resources)

    override fun <T> useResource(f: (R) -> Mono<T>): Mono<T> {
        return LoggingUtils.withMarker { marker ->
            Callable {
                val result = queue.take()
                logger.info(marker, "using resource: $result. queue size: ${queue.size}")
                result
            }
                .blockingToMono()
                .flatMap { client ->
                    f(client)
                        .doOnTerminate {
                            queue.add(client)
                            logger.info("resource $client released. queue size: ${queue.size}")
                        }
                        .doOnCancel {
                            queue.add(client)
                            logger.info("resource $client released. queue size: ${queue.size}")
                        }
                }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BlockingQueueReactiveResources::class.java)
    }
}
