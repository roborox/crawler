package ru.roborox.crawler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.testng.annotations.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.roborox.crawler.http.*
import ru.roborox.crawler.persist.RawRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


class OutOfMemoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var rawRepository: RawRepository
    private val client = WebClient.builder()
        .clientConnector(WebClientHelper.createConnector(10000, 10000))
        .defaultHeaders {
            it.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36")
            it.add(HttpHeaders.ACCEPT, "*/*")
            it.add(HttpHeaders.CACHE_CONTROL, "no-cache")
            it.add(HttpHeaders.CONNECTION, "keep-alive")
        }
        .build()
    private val clientAndProxy = WebClientAndProxy(client)

    private fun doJob(http: HttpClient, clients: WebClients, idx: Int): Mono<Void> {
        return http.getBytes("http://10.7.3.6:8081/nexus/content/repositories/releases/ru/roborox/imslp/crawler/15/crawler-15.rpm", clients = clients)
            .flatMap { resp ->
                val file = Files.createFile(Paths.get("/home/quadro/temp/crawler/$idx.rpm"))
                val channel = Files.newByteChannel(file, StandardOpenOption.WRITE)
                DataBufferUtils.write(resp.body, channel)
//                    .map { DataBufferUtils.release(it) }
                    .then()
                    .map {
                        logger.info("ended")
                    }
            }
            .then()
    }

    @Test(enabled = false)
    fun download() {
        File("/home/quadro/temp/crawler").listFiles()!!.forEach {
            it.delete()
        }
        val http = HttpClientImpl(rawRepository)
        val clients = BlockingQueueReactiveResources(listOf(WebClientAndProxy(client)))

        Flux.fromIterable(0 until 100)
            .flatMap { doJob(http, clients, it) }
            .then()
            .block()
    }

    companion object {
        val logger = LoggerFactory.getLogger(OutOfMemoryTest::class.java)
    }
}