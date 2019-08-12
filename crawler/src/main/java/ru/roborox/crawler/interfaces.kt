package ru.roborox.crawler

import reactor.core.publisher.Mono
import ru.roborox.crawler.domain.Page

interface HasTaskId {
    fun toTaskId(): String
}

interface Loader<in P : HasTaskId> {
    fun load(page: Page, params: P): Mono<LoadResult>
}

interface TaskScheduler {
    fun submit(task: LoaderTask<*, *>): Mono<Void>
}
