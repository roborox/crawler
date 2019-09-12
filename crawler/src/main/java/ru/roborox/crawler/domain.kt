package ru.roborox.crawler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.PageLog
import ru.roborox.crawler.domain.Status
import ru.roborox.crawler.exception.StacktraceUtils
import ru.roborox.logging.reactive.LoggingUtils
import java.util.*

sealed class LoadResult {
    abstract val status: Status

    open fun updatePage(page: Page): Mono<Page> = page.copy(status = status).toMono()
    open fun updateLog(log: PageLog): Mono<PageLog> = log.copy(status = status).toMono()

    class SuccessResult(
        val tasks: List<LoaderTask>
    ) : LoadResult() {

        override val status: Status
            get() = Status.SUCCESS

        override fun updatePage(page: Page): Mono<Page> = page.copy(
            status = status,
            lastLoadDate = Date()
        ).toMono()
    }

    class ErrorResult(private val page: Page, private val exception: Throwable) : LoadResult() {
        override val status: Status
            get() = Status.FAILURE
        override fun updateLog(log: PageLog): Mono<PageLog> {
            return LoggingUtils.withMarker { marker ->
                logger.error(marker, "Got error in page ${page.taskId} loader: ${page.loaderClass}", exception)
                log.copy(
                    status = status,
                    exception = StacktraceUtils.toString(exception)
                ).toMono()
            }
        }
    }

    object SkippedResult : LoadResult() {
        override val status: Status
            get() = Status.SKIPPED
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LoadResult::class.java)
    }
}

data class LoaderTask(
    val taskId: String,
    val loaderClass: Class<out Loader>
)
