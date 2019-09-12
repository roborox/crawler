package ru.roborox.crawler.scheduler

import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Callable

fun <T> Callable<T>.blockingToMono(): Mono<T> {
    return Mono.just(Unit)
        .publishOn(Schedulers.elastic())
        .flatMap {
            try {
                Mono.just(this.call())
            } catch (e: Exception) {
                Mono.error<T>(e)
            }
        }
}
