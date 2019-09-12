package ru.roborox.crawler.domain

import org.springframework.data.domain.Pageable

data class PageRequest(
    val page: Int = 0,
    val size: Int = 50
) {
    fun toPageable(): Pageable {
        return org.springframework.data.domain.PageRequest.of(page, size)
    }
}