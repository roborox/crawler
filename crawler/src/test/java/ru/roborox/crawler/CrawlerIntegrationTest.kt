package ru.roborox.crawler

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.testng.annotations.Test
import ru.roborox.crawler.domain.Page

class CrawlerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var crawler : Crawler

    @Autowired
    private lateinit var testCrawlWithoutMinRate: TestCrawlWithoutMinRate

    @Autowired
    private lateinit var testCrawlWithMinRate: TestCrawlWithMinRate

    @Autowired
    private lateinit var testCrawlNeverReload: TestCrawlNeverReload

    @Autowired
    private lateinit var testCrawlReloadable: TestCrawlReloadable

    @Test(enabled = false)
    fun crawlTestWithoutMinRate() {
        crawler.crawl(null, "ROOT", testCrawlWithoutMinRate).block()
        Thread.sleep(1000)
        val count = mongo.count<Page>(Query(Criteria.where("status").`is`("SUCCESS")
            .and("loaderClass").`is`(AfterTestCrawlZero::class.java.name))).block()!!
        logger.info("count: $count")
        assertTrue(count > 5)
    }

    @Test
    fun crawlTestWithMinRate() {
        crawler.crawl(null, "ROOT", testCrawlWithMinRate).block()
        Thread.sleep(1000)
        val countWithMinRate = mongo.count<Page>(Query(Criteria.where("status").`is`("SUCCESS")
            .and("loaderClass").`is`(AfterTestCrawlWithMinRate::class.java.name))).block()!!
        logger.info("countWithMinRate: $countWithMinRate")
        assertTrue(countWithMinRate < 4)
    }

    @Test
    fun crawlTestReloadable() {
        crawler.crawl(null, "ROOT", testCrawlReloadable).block()
        Thread.sleep(1000)
        val page1 = mongo.find<Page>(Query(Criteria.where("loaderClass").`is`(AfterTestCrawlReloadable::class.java.name))).blockFirst()!!
        crawler.crawl(null, "ROOT", testCrawlReloadable).block()
        Thread.sleep(1000)
        val page2 = mongo.find<Page>(Query(Criteria.where("loaderClass").`is`(AfterTestCrawlReloadable::class.java.name))).blockFirst()!!
        logger.info("page1: $page1")
        logger.info("page2: $page2")
        assertTrue(page1.lastUpdate!! < page2.lastUpdate)
    }

    @Test
    fun crawlTestNeverReload() {
        crawler.crawl(null, "ROOT", testCrawlNeverReload).block()
        Thread.sleep(1000)
        val page1 = mongo.find<Page>(Query(Criteria.where("loaderClass").`is`(AfterTestCrawlNeverReload::class.java.name))).blockFirst()!!
        logger.info("page1: $page1")
        crawler.crawl(null, "ROOT", testCrawlNeverReload).block()
        Thread.sleep(1000)
        val page2 = mongo.find<Page>(Query(Criteria.where("loaderClass").`is`(AfterTestCrawlNeverReload::class.java.name))).blockFirst()!!

        logger.info("page1: $page1")
        logger.info("page2: $page2")
        assertEquals(page1.lastUpdate, page2.lastUpdate)
    }
}