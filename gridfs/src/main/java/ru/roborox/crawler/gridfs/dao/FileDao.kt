package ru.roborox.crawler.gridfs.dao

import com.mongodb.client.gridfs.model.GridFSFile
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.roborox.logging.reactive.LoggingUtils

@Component
class FileDao(
    private val bucket: GridFSBucket
) {
    fun saveFile(fileName: String, contentType: String, bufferFlux: List<DataBuffer>): Mono<ObjectId> {
        return LoggingUtils.withMarker { marker ->
            logger.info(marker, "saveFile name: $fileName contentType: $contentType")
            val options = GridFSUploadOptions().metadata(Document("content-type", contentType))

            val byteBuffers = bufferFlux.map(DataBuffer::asByteBuffer)
            bucket.uploadFromStream(fileName, ByteBufferAsyncInputStream(byteBuffers), options).toMono()
        }
    }

    fun exists(id: ObjectId): Mono<Boolean> {
        return getFile(id)
            .map { true }
            .switchIfEmpty(false.toMono())
    }

    fun getFile(id: ObjectId): Mono<GridFSFile> {
        return bucket.find(Document("_id", id)).toMono()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(FileDao::class.java)
    }
}