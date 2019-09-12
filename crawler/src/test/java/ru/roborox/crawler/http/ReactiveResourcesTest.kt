package ru.roborox.crawler.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import reactor.core.publisher.Mono
import java.time.Duration

class ReactiveResourcesTest {

    @Test
    fun testBlock() {
        testResources(listOf(1)) {
            assertTrue(it >= TIMEOUT * 2)
        }
        testResources(listOf(1, 2)) {
            assertTrue(it >= TIMEOUT)
            assertTrue(it < TIMEOUT * 2)
        }
    }

    private fun testResources(resources: Collection<Int>, verifier: (Long) -> Unit) {
        val res = BlockingQueueReactiveResources(resources)
        val start = System.currentTimeMillis()
        Mono.`when`(
            res.useResource { Mono.delay(Duration.ofMillis(TIMEOUT.toLong())) },
            res.useResource { Mono.delay(Duration.ofMillis(TIMEOUT.toLong())) }
        ).block()
        val end = System.currentTimeMillis()
        logger.info("Time: ${end - start}ms for: $resources")
        verifier(end - start)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ReactiveResourcesTest::class.java)
        private const val TIMEOUT = 200
    }
}