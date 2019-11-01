package ru.roborox.crawler.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@EnableRoboroxApi(scanPackage = [MockContext::class])
class MockContext

@RestController
class TestController {
    @GetMapping("/test")
    fun test(): String {
        return "hello"
    }
}