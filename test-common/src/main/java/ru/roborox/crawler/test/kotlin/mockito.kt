package ru.roborox.crawler.test.kotlin

import org.mockito.ArgumentMatcher
import org.mockito.Mockito

@Suppress("UNCHECKED_CAST")
fun <T> argumentThat(test: (T) -> Boolean): T {
    return Mockito.argThat(object : ArgumentMatcher<T>() {
        override fun matches(argument: Any?): Boolean = test(argument as T)
    })
}
