package ru.roborox.crawler

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import ru.roborox.crawler.persist.Persist
import ru.roborox.crawler.persist.configuration.EnableRoboroxMongo
import ru.roborox.crawler.persist.configuration.IncludePersistProperties
import ru.roborox.crawler.quartz.EnableRoboroxQuartz

@Configuration
@EnableRoboroxQuartz
@ComponentScan(basePackageClasses = [CrawlerConfiguration::class], excludeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [Configuration::class])])
@Import(
    PersistConfiguration::class
)
class CrawlerConfiguration

@EnableRoboroxMongo(repositoriesPackage = [PersistConfiguration::class])
@IncludePersistProperties
class PersistConfiguration