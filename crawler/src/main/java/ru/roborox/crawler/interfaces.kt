package ru.roborox.crawler

import reactor.core.publisher.Mono
import ru.roborox.crawler.domain.Page

interface Loader {
    fun load(page: Page): Mono<LoadResult>
}

interface TaskScheduler {
    fun submit(task: LoaderTask): Mono<Void>
}
