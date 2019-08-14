package ru.roborox.crawler

import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.PageLog
import ru.roborox.crawler.domain.Status
import ru.roborox.crawler.exception.StacktraceUtils
import java.util.*

sealed class LoadResult {
    abstract val status: Status

    open fun updatePage(page: Page): Page = page.copy(status = status)
    open fun updateLog(log: PageLog): PageLog = log.copy(status = status)

    class SuccessResult(
        val tasks: List<LoaderTask<*, *>>
    ) : LoadResult() {

        override val status: Status
            get() = Status.SUCCESS

        override fun updatePage(page: Page): Page = page.copy(
            status = status,
            lastLoadDate = Date()
        )
    }

    class ErrorResult(val exception: Throwable) : LoadResult() {
        override val status: Status
            get() = Status.FAILURE

        override fun updateLog(log: PageLog): PageLog = log.copy(
            status = status,
            exception = StacktraceUtils.toString(exception)
        )
    }

    object SkippedResult : LoadResult() {
        override val status: Status
            get() = Status.SKIPPED
    }
}

data class LoaderTask<P : HasTaskId, L : Loader<P>>(
    val params: P,
    val loaderClass: Class<L>
)

object Root : HasTaskId {
    override fun toTaskId(): String = "ROOT"
}

data class StringParam(val value: String) : HasTaskId {
    override fun toTaskId(): String = value
    override fun toString(): String = value
}

fun String.toParam() = StringParam(this)