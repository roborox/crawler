package ru.roborox.crawler.proxy

import org.bson.types.ObjectId
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ProxyRepository : ReactiveCrudRepository<Proxy, ObjectId> {
    fun findByProvider(provider: ProxyProvider)
    fun findByType(type: reactor.netty.tcp.ProxyProvider.Proxy)
}