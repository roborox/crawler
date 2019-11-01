package ru.roborox.crawler.gridfs.dao

import com.mongodb.client.gridfs.model.GridFSFile
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsOperations
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.roborox.logging.reactive.LoggingUtils

@Component
class FileDao(
    private val gridfs: ReactiveGridFsOperations
) {
    fun saveFile(fileName: String, contentType: String, content: Flux<DataBuffer>): Mono<ObjectId> {
        return LoggingUtils.withMarker { marker ->
            logger.info(marker, "saveFile name: $fileName contentType: $contentType")
            gridfs.store(content, fileName, contentType)
        }
    }

    fun exists(id: ObjectId): Mono<Boolean> {
        return getFile(id)
            .map { true }
            .switchIfEmpty(false.toMono())
    }

    fun load(file: GridFSFile): Mono<ReactiveGridFsResource> {
        return gridfs.getResource(file)
    }

    fun getFile(id: ObjectId): Mono<GridFSFile> {
        return gridfs.findOne(Query(Criteria.where("_id").`is`(id)))
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(FileDao::class.java)
    }
}