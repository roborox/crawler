package ru.roborox.crawler.http

import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import org.testng.annotations.Test
import ru.roborox.crawler.AbstractIntegrationTest
import ru.roborox.crawler.domain.Raw
import ru.roborox.crawler.persist.RawRepository

class ValidateTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var rawRepository: RawRepository

    @Test
    fun validateHtml() {
        val client = HttpClientImpl(rawRepository)
        try {
            client.get("https://roborox.org/some-error-url")
                .validateHtml(1) { it.contains("some value") }
                .block()
            fail()
        } catch (e: ValidationException) {
            //expected
        }
    }

    @Test
    fun validateSuccess() {
        val client = HttpClientImpl(rawRepository)
        client.get("https://roborox.org/some-error-url")
            .validateHtml(2) { it.contains("Not Found") }
            .block()
    }

    @Test
    fun validateSuccessWithRaw() {
        rawRepository.save(Raw(id = "some-error-url", content = "123 Not Found")).block()
        val client = HttpClientImpl(rawRepository)
        client.get("https://roborox.org/some-error-url", useRaw = true)
            .validateHtml(2) { it.contains("Not Found") }
            .block()
        assertTrue(rawRepository.findById("some-error-url").block()!!.content.equals("123 Not Found"))
    }
}