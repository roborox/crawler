package ru.roborox.crawler.proxy

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

enum class ProxyProvider {
    FINEPROXY
}

data class Proxy(
    val host: String,
    val port: Int,
    val provider: ProxyProvider,
    val type: reactor.netty.tcp.ProxyProvider.Proxy,
    @Id
    val id: ObjectId = ObjectId.get()
) {
    override fun toString(): String {
        return "$host:$port"
    }
}