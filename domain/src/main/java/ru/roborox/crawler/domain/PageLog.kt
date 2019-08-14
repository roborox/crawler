package ru.roborox.crawler.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "page_log")
data class PageLog(
    val pageId: ObjectId,
    val status: Status,
    val exception: String? = null,
    @Id
    val id: ObjectId = ObjectId.get(),
    @CreatedDate
    val createDate: Date? = null,
    @LastModifiedDate
    val updateDate: Date? = null
)