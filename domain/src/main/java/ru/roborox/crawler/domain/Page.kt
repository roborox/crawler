package ru.roborox.crawler.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

enum class Status {
    LOADING,
    SKIPPED,
    SUCCESS,
    FAILURE
}

@Document(collection = "page")
@CompoundIndexes(
    CompoundIndex(def = "{loaderClass: 1, taskId: 1}", unique = true, background = true)
)
data class Page(
    val loaderClass: String,
    val taskId: String,
    val status: Status,
    val lastLoadAttempt: Date,
    val lastLoadDate: Date? = null,
    @LastModifiedDate
    val lastUpdate: Date? = null,
    @CreatedDate
    val createDate: Date? = null,
    @Id
    val id: ObjectId = ObjectId.get(),
    @Version
    val version: Long? = null
)
