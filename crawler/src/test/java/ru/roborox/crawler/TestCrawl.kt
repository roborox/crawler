package ru.roborox.crawler

import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.roborox.crawler.anotation.Duration
import ru.roborox.crawler.anotation.PageLoader
import ru.roborox.crawler.anotation.Reload
import ru.roborox.crawler.domain.LoaderTask
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.ReloadType
import java.util.*
import java.util.concurrent.TimeUnit

@PageLoader
class TestCrawlWithoutMinRate : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(manyLoaderTask()).toMono()
    }

    fun manyLoaderTask() : List<LoaderTask> {
        val list = ArrayList<LoaderTask>()
        for (i in 1 .. 100) {
            list.add(AfterTestCrawlZero::class.newTask("Test$i"))
        }
        return list
    }
}

@PageLoader
class TestCrawlWithMinRate : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(manyLoaderTask()).toMono()
    }

    fun manyLoaderTask() : List<LoaderTask> {
        val list = ArrayList<LoaderTask>()
        for (i in 1 .. 100) {
            list.add(AfterTestCrawlWithMinRate::class.newTask("TestMinRate$i"))
        }
        return list
    }
}

@PageLoader
class TestCrawlReloadable : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(listOf(AfterTestCrawlReloadable::class.newTask("TestReload"))).toMono()
    }
}

abstract class AfterTestCrawl : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(emptyList()).toMono()
    }
}

@PageLoader
class AfterTestCrawlZero : AfterTestCrawl()

@PageLoader(minRate = Duration(value = 350))
class AfterTestCrawlWithMinRate: AfterTestCrawl()

@PageLoader(reload = Reload(value = ReloadType.AUTO, rate = Duration(value = 300, unit = TimeUnit.MILLISECONDS)))
class AfterTestCrawlReloadable : AfterTestCrawl()


@PageLoader(minRate = Duration(value = 0))
class TestCrawlNeverReload : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(listOf(AfterTestCrawlNeverReload::class.newTask("neverReload"))).toMono()
    }
}

@PageLoader(reload = Reload(value = ReloadType.NEVER))
class AfterTestCrawlNeverReload : AfterTestCrawl()
