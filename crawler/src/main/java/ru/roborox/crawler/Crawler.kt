package ru.roborox.crawler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import ru.roborox.logging.reactive.LoggingUtils
import ru.roborox.logging.reactive.ReactiveLoggerContext
import java.util.*

@Service
class Crawler(
    private val pageRepository: PageRepository,
    private val pageLogRepository: PageLogRepository,
    private val taskScheduler: TaskScheduler
) {
    fun crawl(taskId: String, loader: Loader): Mono<Void> {
        return LoggingUtils.withMarker { marker ->
            logger.info(marker, "crawl ${loader.javaClass.name} $taskId")
            markLoading(loader.javaClass.name, taskId)
                .flatMap { page ->
                    load(page, loader)
                }
                .then()
        }.subscriberContext { ReactiveLoggerContext.add(it, mapOf("taskId" to taskId, "loaderClass" to loader.javaClass.name)) }
    }

    private fun load(page: Page, loader: Loader): Mono<Void> {
        return pageLogRepository.save(PageLog(page.id, Status.LOADING))
            .flatMap { log ->
                loader.load(page)
                    .flatMap { scheduleTasks(it) }
                    .onErrorResume { LoadResult.ErrorResult(page, it).toMono() }
                    .flatMap { result ->
                        Mono.`when`(
                            result.updatePage(page).flatMap { pageRepository.save(it) },
                            result.updateLog(log).flatMap { pageLogRepository.save(it) }
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

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Crawler::class.java)
    }
}
