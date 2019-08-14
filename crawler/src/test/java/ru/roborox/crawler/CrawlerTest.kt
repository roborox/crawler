package ru.roborox.crawler

import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.bson.types.ObjectId
import org.mockito.Matchers
import org.mockito.Mockito.*
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.PageLog
import ru.roborox.crawler.domain.Status
import ru.roborox.crawler.persist.PageLogRepository
import ru.roborox.crawler.persist.PageRepository
import ru.roborox.crawler.test.kotlin.argumentThat
import java.util.*

@Test
class CrawlerTest {
    private val pageRepository = mock(PageRepository::class.java)
    private val pageLogRepository = mock(PageLogRepository::class.java)
    private val taskScheduler = mock(TaskScheduler::class.java)
    private val crawler = Crawler(pageRepository, pageLogRepository, taskScheduler)

    fun crawlNewPage() {
        val id = randomAlphabetic(10)
        val pageId = ObjectId.get()
        val logId = ObjectId.get()

        `when`(pageRepository.findByLoaderClassAndTaskId(TestLoader::class.java.name, id))
            .thenReturn(Mono.empty())
        `when`(pageRepository.save(Matchers.any(Page::class.java)))
            .thenAnswer { (it.arguments[0] as Page).copy(id = pageId).toMono() }
        `when`(pageLogRepository.save(Matchers.any(PageLog::class.java)))
            .thenAnswer { (it.arguments[0] as PageLog).copy(id = logId).toMono() }

        crawler.crawl(id.toParam(), TestLoader(LoadResult.SuccessResult(listOf()))).block()

        verify(pageRepository).save(argumentThat {
            it.status == Status.LOADING
        })
        verify(pageRepository).save(argumentThat {
            it.status == Status.SUCCESS && it.id == pageId
        })
        verify(pageLogRepository).save(argumentThat {
            it.status == Status.LOADING
        })
        verify(pageLogRepository).save(argumentThat {
            it.status == Status.SUCCESS && it.id == logId
        })
        verify(pageRepository).findByLoaderClassAndTaskId(TestLoader::class.java.name, id)
    }

    fun crawlExistingPage() {
        val id = randomAlphabetic(10)
        val pageId = ObjectId.get()
        val logId = ObjectId.get()

        `when`(pageRepository.findByLoaderClassAndTaskId(TestLoader::class.java.name, id))
            .thenReturn(Mono.just(Page(TestLoader::class.java.name, id, Status.FAILURE, Date(), id = pageId)))
        `when`(pageRepository.save(Matchers.any(Page::class.java)))
            .thenAnswer { (it.arguments[0] as Page).toMono() }
        `when`(pageLogRepository.save(Matchers.any(PageLog::class.java)))
            .thenAnswer { (it.arguments[0] as PageLog).copy(id = logId).toMono() }

        crawler.crawl(id.toParam(), TestLoader(LoadResult.SuccessResult(listOf()))).block()

        verify(pageRepository).save(argumentThat {
            it.status == Status.LOADING
        })
        verify(pageRepository).save(argumentThat {
            it.status == Status.SUCCESS && it.id == pageId
        })
        verify(pageLogRepository).save(argumentThat {
            it.status == Status.LOADING
        })
        verify(pageLogRepository).save(argumentThat {
            it.status == Status.SUCCESS && it.id == logId
        })
        verify(pageRepository).findByLoaderClassAndTaskId(TestLoader::class.java.name, id)
    }

    fun scheduleTasks() {
        val id = randomAlphabetic(10)
        val pageId = ObjectId.get()
        val logId = ObjectId.get()

        `when`(pageRepository.findByLoaderClassAndTaskId(TestLoader::class.java.name, id))
            .thenReturn(Mono.empty())
        `when`(pageRepository.save(Matchers.any(Page::class.java)))
            .thenAnswer { (it.arguments[0] as Page).copy(id = pageId).toMono() }
        `when`(pageLogRepository.save(Matchers.any(PageLog::class.java)))
            .thenAnswer { (it.arguments[0] as PageLog).copy(id = logId).toMono() }
        val nextId = randomAlphabetic(10)
        `when`(taskScheduler.submit(LoaderTask(nextId.toParam(), TestLoader::class.java)))
            .thenReturn(Mono.empty())

        crawler.crawl(id.toParam(), TestLoader(LoadResult.SuccessResult(listOf(LoaderTask(nextId.toParam(), TestLoader::class.java))))).block()

        verify(pageRepository).save(argumentThat {
            it.status == Status.LOADING
        })
        verify(pageRepository).save(argumentThat {
            it.status == Status.SUCCESS && it.id == pageId
        })
        verify(pageLogRepository).save(argumentThat {
            it.status == Status.LOADING
        })
        verify(pageLogRepository).save(argumentThat {
            it.status == Status.SUCCESS && it.id == logId
        })
        verify(pageRepository).findByLoaderClassAndTaskId(TestLoader::class.java.name, id)
        verify(taskScheduler).submit(LoaderTask(nextId.toParam(), TestLoader::class.java))
    }

    @BeforeMethod
    fun before() {
        reset(pageRepository, pageLogRepository, taskScheduler)
    }

    @AfterMethod
    fun after() {
        verifyNoMoreInteractions(pageRepository, pageLogRepository, taskScheduler)
    }
}

class TestLoader(private val loadResult: LoadResult) : Loader<StringParam> {
    override fun load(page: Page, params: StringParam): Mono<LoadResult> = loadResult.toMono()
}