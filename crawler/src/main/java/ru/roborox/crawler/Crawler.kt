package ru.roborox.crawler

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.publisher.toMono
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.Status
import ru.roborox.crawler.persist.PageRepository
import java.util.*

@Service
class Crawler(
    private val pageRepository: PageRepository,
    private val taskScheduler: TaskScheduler
) {
    fun <P : HasTaskId> crawl(params: P, loader: Loader<P>): Mono<Void> {
        return markLoading(loader.javaClass.name, params.toTaskId())
            .flatMap { page ->
                loader.load(page, params)
                    .flatMap {
                        when (it) {
                            is LoadResult.SuccessResult ->
                                submitAll(it)
                                    .thenReturn(page.copy(status = Status.SUCCESS))
                            is LoadResult.ErrorResult ->
                                page.copy(status = Status.FAILURE).toMono()
                            else ->
                                page.copy(status = Status.SKIPPED).toMono()
                        }
                    }
                    .flatMap { pageRepository.save(it) }
            }
            .then()
    }

    private fun submitAll(result: LoadResult.SuccessResult): Mono<Void> {
        return Flux.fromIterable(result.tasks)
            .flatMap { taskScheduler.submit(it) }
            .then()
    }

    private fun markLoading(loaderClass: String, taskId: String): Mono<Page> {
        return pageRepository.findByLoaderClassAndTaskId(loaderClass, taskId)
            .flatMap { pageRepository.save(it.copy(status = Status.LOADING, lastLoadAttempt = Date())) }
            .switchIfEmpty { pageRepository.save(Page(loaderClass, taskId, status = Status.LOADING, lastLoadAttempt = Date())) }
    }
}
