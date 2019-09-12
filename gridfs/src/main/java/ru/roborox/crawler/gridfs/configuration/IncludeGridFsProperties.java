package ru.roborox.crawler.gridfs.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(GridFsPropertiesConfiguration.class)
public @interface IncludeGridFsProperties {
}

@Configuration
@PropertySource(value = {"classpath:/gridfs.properties", "classpath:/gridfs-test.properties", "file:../conf/gridfs.properties"}, ignoreResourceNotFound = true)
class GridFsPropertiesConfiguration {
}
