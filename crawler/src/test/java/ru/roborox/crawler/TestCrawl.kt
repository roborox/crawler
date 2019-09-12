package ru.roborox.crawler

import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.roborox.crawler.anotation.Duration
import ru.roborox.crawler.anotation.PageLoader
import ru.roborox.crawler.anotation.Reload
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.ReloadType
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@PageLoader
class TestCrawlWithoutMinRate : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(manyLoaderTask()).toMono()
    }

    fun manyLoaderTask() : List<LoaderTask> {
        val list = ArrayList<LoaderTask>()
        for (i in 1 .. 100) {
            list.add(LoaderTask("Test$i", AfterTestCrawlZero::class.java))
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
            list.add(LoaderTask("TestMinRate$i", AfterTestCrawlWithMinRate::class.java))
        }
        return list
    }
}

@PageLoader
class TestCrawlReloadable : Loader {
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(Collections.singletonList(LoaderTask("TestReload", AfterTestCrawlReloadable::class.java))).toMono()
    }
}

abstract class AfterTestCrawl : Loader {
    public var counter = AtomicInteger(0)
    override fun load(page: Page): Mono<LoadResult> {
        return LoadResult.SuccessResult(emptyList())
            .toMono<LoadResult>()
            .doOnSubscribe {counter.getAndIncrement()}
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
        return LoadResult.SuccessResult(Collections.singletonList(LoaderTask("TestReload", AfterTestCrawlNeverReload::class.java))).toMono()
    }
}

@PageLoader(reload = Reload(value = ReloadType.NEVER))
class AfterTestCrawlNeverReload : AfterTestCrawl()
