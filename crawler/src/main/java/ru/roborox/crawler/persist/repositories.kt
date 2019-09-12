package ru.roborox.crawler.persist

import org.bson.types.ObjectId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import ru.roborox.crawler.domain.Page
import ru.roborox.crawler.domain.PageLog
import ru.roborox.crawler.domain.Raw

interface PageRepository : ReactiveCrudRepository<Page, ObjectId> {
    fun findByLoaderClassAndTaskId(loaderClass: String, taskId: String): Mono<Page>
    fun findTopByLoaderClassAndNextStartDateIsNotNullOrderByNextStartDateAsc(loaderClass: String) : Mono<Page>
}

interface PageLogRepository : ReactiveCrudRepository<PageLog, ObjectId>

interface RawRepository : ReactiveCrudRepository<Raw, String>