package ru.roborox.crawler.http

import io.netty.channel.ConnectTimeoutException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.publisher.toMono
import ru.roborox.crawler.http.Validate.logger
import ru.roborox.logging.reactive.LoggingUtils

fun <T, R> Mono<T>.validate(numRetries: Long, validator: (T) -> Mono<R>): Mono<R> {
    return LoggingUtils.withMarker { marker ->
        this.flatMap(validator)
            .switchIfEmpty {
                logger.warn(marker, "no response")
                Mono.error(ValidationException("no response"))
            }
            .onErrorResume(ConnectTimeoutException::class.java) {
                Mono.error(ValidationException(cause = it))
            }
            .retry(numRetries) { it is ValidationException }
    }
}

fun Mono<HttpResponse<String?>>.validateHtml(numRetries: Long, validator: (String) -> Boolean): Mono<HttpResponse<String>> {
    return LoggingUtils.withMarker { marker ->
        this.validate(numRetries) { response ->
            if (response.body == null) {
                logger.warn(marker, "body not found: $response")
                Mono.error(ValidationException("http body is not provided"))
            } else {
                if (validator(response.body)) {
                    HttpResponse(response.status, response.headers, response.body).toMono()
                } else {
                    Mono.error(ValidationException("not validated: ${response.body}"))
                }
            }
        }
    }
}

class ValidationException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

object Validate {
    val logger: Logger = LoggerFactory.getLogger(Validate::class.java)
}