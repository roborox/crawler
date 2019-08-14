package ru.roborox.crawler.gridfs.dao

import com.mongodb.reactivestreams.client.Success
import com.mongodb.reactivestreams.client.gridfs.AsyncInputStream
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

class ByteBufferAsyncInputStream(
    private val source: List<ByteBuffer>
) : AsyncInputStream {

    private var pos =  -1

    override fun close(): Publisher<Success> {
        return Mono.just(Success.SUCCESS)
    }

    override fun read(dst: ByteBuffer?): Publisher<Int> {
        pos++
        return if (pos <= source.size - 1) {
            val current = source[pos]
            val remaining = current.remaining()
            dst?.put(current)
            Mono.just(if (remaining <= 0) -1 else remaining)
        } else {
            Mono.just(-1)
        }
    }
}