package ru.roborox.crawler.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

fun ObjectMapper.withModule(postprocessor: PostProcessor<SimpleModule>): ObjectMapper {
    val module = SimpleModule()
    postprocessor.postprocess(module)
    this.registerModule(module)
    return this
}

object ObjectMapperUtils {
    @JvmStatic
    fun defaultObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        return mapper
    }
}

@Configuration
class DefaultObjectMapperConfiguration {
    @Bean
    fun mapper() = ObjectMapperUtils.defaultObjectMapper()
}