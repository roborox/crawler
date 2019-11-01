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
    NEW,
    SCHEDULED,
    LOADING,
    SKIPPED,
    SUCCESS,
    FAILURE
}

@Document(collection = "page")
@CompoundIndexes(
    CompoundIndex(def = "{loaderClass: 1, taskId: 1}", unique = true, background = true),
    CompoundIndex(def = "{loaderClass: 1, nextStartDate: 1}", background = true)
)
data class Page(
    val loaderClass: String,
    val taskId: String,
    val parent: LoaderTask?,
    val status: Status,
    val lastLoadAttempt: Date? = null,
    val nextStartDate: Date? = null,
    val lastLoadDate: Date? = null,
    val reparse: Boolean? = null,
    @LastModifiedDate
    val lastUpdate: Date? = null,
    @CreatedDate
    val createDate: Date? = null,
    @Id
    val id: ObjectId = ObjectId.get(),
    @Version
    val version: Long? = null
)

data class LoaderTask(
    val taskId: String,
    val loaderClass: String
)
