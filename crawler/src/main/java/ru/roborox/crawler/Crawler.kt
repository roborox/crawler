package ru.roborox.crawler

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.publisher.toMono
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.PageLog
import ru.roborox.crawler.domain.Status
import ru.roborox.crawler.persist.PageLogRepository
import ru.roborox.crawler.persist.PageRepository
import java.util.*

@Service
class Crawler(
    private val pageRepository: PageRepository,
    private val pageLogRepository: PageLogRepository,
    private val taskScheduler: TaskScheduler
) {
    fun <P : HasTaskId> crawl(params: P, loader: Loader<P>): Mono<Void> {
        return markLoading(loader.javaClass.name, params.toTaskId())
            .flatMap { page ->
                load(page, params, loader)
            }
            .then()
    }

    private fun <P : HasTaskId> load(page: Page, params: P, loader: Loader<P>): Mono<Void> {
        return pageLogRepository.save(PageLog(page.id, Status.LOADING))
            .flatMap { log ->
                loader.load(page, params)
                    .flatMap { scheduleTasks(it) }
                    .onErrorResume { LoadResult.ErrorResult(it).toMono() }
                    .flatMap { result ->
                        Mono.`when`(
                            pageRepository.save(result.updatePage(page)),
                            pageLogRepository.save(result.updateLog(log))
                        )
                    }
            }
    }

    private fun scheduleTasks(result: LoadResult): Mono<LoadResult> {
        return if (result is LoadResult.SuccessResult) {
            submitAll(result)
                .thenReturn(result)
        } else {
            result.toMono()
        }
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
