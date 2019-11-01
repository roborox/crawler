package ru.roborox.crawler.gridfs

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import ru.roborox.crawler.gridfs.configuration.EnableRoboroxGridFs
import ru.roborox.crawler.persist.configuration.EnableRoboroxMongo

@Configuration
@EnableRoboroxGridFs
@EnableRoboroxMongo
@PropertySource(value = ["classpath:/app.properties"])
class MockContext