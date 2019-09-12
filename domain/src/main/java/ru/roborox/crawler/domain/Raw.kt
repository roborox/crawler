package ru.roborox.crawler.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "raw")
data class Raw(
    @Id
    val id: String,
    val content: String
)