package ru.roborox.crawler.api

import org.springframework.test.context.ContextConfiguration
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod

@ContextConfiguration(classes = [MockContext::class])
abstract class AbstractIntegrationTest : ru.roborox.crawler.test.AbstractIntegrationTest() {
    @BeforeClass
    override fun setUp() {
        super.setUp()
    }

    @BeforeMethod
    override fun before() {
        super.before()
    }
}