package ru.roborox.crawler.persist

import org.bson.types.ObjectId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import ru.roborox.crawler.domain.Page

object Persist

interface PageRepository : ReactiveCrudRepository<Page, ObjectId> {
    fun findByLoaderClassAndTaskId(loaderClass: String, taskId: String): Mono<Page>
}