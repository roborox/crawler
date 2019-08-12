package ru.roborox.crawler.kotlin

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

fun <K, V> Map<K, V>.toMuliValueMap(): MultiValueMap<K, V> {
    val result = LinkedMultiValueMap<K, V>()
    for (entry in entries) {
        result[entry.key] = listOf(entry.value)
    }
    return result
}