package ru.roborox.crawler.quartz.config

import org.springframework.context.annotation.Configuration
import ru.roborox.crawler.persist.configuration.EnableRoboroxMongo
import ru.roborox.crawler.persist.configuration.IncludePersistProperties

@Configuration
@EnableRoboroxMongo
@IncludePersistProperties
class MockConfiguration