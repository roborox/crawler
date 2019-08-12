package ru.roborox.crawler

import ru.roborox.crawler.domain.Status

sealed class LoadResult {
    abstract val status: Status

    class SuccessResult(
        val tasks: List<LoaderTask<*, *>>
    ) : LoadResult() {

        override val status: Status
            get() = Status.SUCCESS
    }

    //todo
    class ErrorResult(val exception: Exception) : LoadResult() {
        override val status: Status
            get() = Status.FAILURE
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