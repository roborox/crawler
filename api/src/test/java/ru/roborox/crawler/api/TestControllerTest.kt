package ru.roborox.crawler.api

import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.testng.Assert.assertEquals

class TestControllerTest : AbstractIntegrationTest() {
    fun testHello() {
        val test: String = RestTemplate().getForObject("$baseUrl/test")
        assertEquals(test, "hello")
    }
}