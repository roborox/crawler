package ru.roborox.crawler.extract

import java.util.regex.Matcher
import java.util.regex.Pattern

fun <T> String.byRegex(pattern: Pattern, extractor: (Matcher) -> T): Sequence<T> {
    return sequence {
        val m = pattern.matcher(this@byRegex)
        while (m.find()) {
            yield(extractor(m))
        }
    }
}