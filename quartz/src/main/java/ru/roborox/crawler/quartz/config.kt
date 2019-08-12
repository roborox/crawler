package ru.roborox.crawler.quartz

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import java.util.*

@Configuration
class QuartzConfiguration {
    @Value("\${mongoUrls}")
    private lateinit var mongoAddresses: String
    @Value("\${mongoDatabase}")
    private lateinit var mongoDatabase: String
    @Value("\${quartzThreadCount:1}")
    private lateinit var threadCount: String

    @Bean
    fun quartzShedulerFactory(applicationContext: ApplicationContext): SchedulerFactoryBean {
        val bean = SchedulerFactoryBean()
        val props = Properties()
        props["org.quartz.scheduler.skipUpdateCheck"] = "true"
        props["org.quartz.jobStore.class"] = "com.novemberain.quartz.mongodb.MongoDBJobStore"
        props["org.quartz.jobStore.addresses"] = mongoAddresses
        props["org.quartz.jobStore.dbName"] = mongoDatabase
        props["org.quartz.threadPool.threadCount"] = threadCount
        props["org.quartz.jobStore.collectionPrefix"] = "quartz"
        bean.setQuartzProperties(props)
        bean.setJobFactory(SpringBeanJobFactory())
        bean.isAutoStartup = true
        return bean
    }
}