package ru.roborox.crawler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import ru.roborox.crawler.domain.LoaderTask
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
    fun crawl(parent: LoaderTask?, taskId: String, loader: Loader): Mono<Void> {
        return LoggingUtils.withMarker { marker ->
            logger.info(marker, "crawl ${loader.javaClass.name} $taskId")
            markLoading(parent, loader.javaClass.name, taskId)
                .flatMap { page ->
                    load(page, loader)
                }
                .then()
        }.subscriberContext { ReactiveLoggerContext.add(it, mapOf("taskId" to taskId, "loaderClass" to loader.javaClass.name)) }
    }

    private fun load(page: Page, loader: Loader): Mono<Void> {
        val thisTask = LoaderTask(page.taskId, loader.javaClass.name)
        return pageLogRepository.save(PageLog(page.id, Status.LOADING))
            .flatMap { log ->
                loader.load(page)
                    .flatMap { scheduleNext(thisTask, it) }
                    .onErrorResume { LoadResult.ErrorResult(page, it).toMono() }
                    .flatMap { result ->
                        Mono.`when`(
                            result.updatePage(page).flatMap { pageRepository.save(it) },
                            result.updateLog(log).flatMap { pageLogRepository.save(it) }
                        )
                    }
            }
    }

    private fun scheduleNext(parent: LoaderTask, result: LoadResult): Mono<LoadResult> {
        return if (result is LoadResult.SuccessResult) {
            submitAll(parent, result)
                .thenReturn(result)
        } else {
            result.toMono()
        }
    }

    private fun submitAll(parent: LoaderTask, result: LoadResult.SuccessResult): Mono<Void> {
        return Flux.fromIterable(result.tasks)
            .flatMap { task ->
                createOrUpdatePage(parent, task)
                    .flatMap { taskScheduler.submit(task) }
            }
            .then()
    }

    private fun createOrUpdatePage(parent: LoaderTask, task: LoaderTask): Mono<Page> {
        val (taskId, loaderClass) = task
        return pageRepository.findByLoaderClassAndTaskId(loaderClass, taskId)
            .switchIfEmpty {
                logger.info("creating new page $task")
                pageRepository.save(Page(loaderClass, taskId, parent, status = Status.NEW))
            }
            .onErrorResume {
                if (it is OptimisticLockingFailureException || it is DuplicateKeyException) {
                    Mono.empty()
                } else {
                    Mono.error(it)
                }
            }
    }

    private fun markLoading(parent: LoaderTask?, loaderClass: String, taskId: String): Mono<Page> {
        return pageRepository.findByLoaderClassAndTaskId(loaderClass, taskId)
            .flatMap { pageRepository.save(it.copy(status = Status.LOADING, lastLoadAttempt = Date())) }
            .switchIfEmpty { pageRepository.save(Page(loaderClass, taskId, parent, status = Status.LOADING, lastLoadAttempt = Date())) }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Crawler::class.java)
    }
}
