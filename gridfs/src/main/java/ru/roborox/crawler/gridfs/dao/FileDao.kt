package ru.roborox.crawler.gridfs.dao

import com.mongodb.client.gridfs.model.GridFSFile
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@Component
class FileDao(
    private val bucket: GridFSBucket
) {
    fun saveFile(fileName: String, contentType: String, bufferFlux: Flux<DataBuffer>): Mono<ObjectId> {
        val options = GridFSUploadOptions().metadata(Document("content-type", contentType))

        return bufferFlux
            .map(DataBuffer::asByteBuffer)
            .collectList()
            .flatMap { bucket.uploadFromStream(fileName, ByteBufferAsyncInputStream(it), options).toMono() }
    }

    fun exists(id: ObjectId): Mono<Boolean> {
        return getFile(id)
            .map { true }
            .switchIfEmpty(false.toMono())
    }

    fun getFile(id: ObjectId): Mono<GridFSFile> {
        return bucket.find(Document("_id", id)).toMono()
    }
}