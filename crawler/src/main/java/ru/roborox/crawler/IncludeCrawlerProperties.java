package ru.roborox.crawler;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CrawlerPropertiesConfiguration.class)
public @interface IncludeCrawlerProperties {
}

@Configuration
@PropertySource(value = {"classpath:/crawler.properties", "classpath:/crawler-test.properties", "file:../conf/crawler.properties"}, ignoreResourceNotFound = true)
class CrawlerPropertiesConfiguration {
}
