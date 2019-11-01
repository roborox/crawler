package ru.roborox.crawler.gridfs.configuration;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

@Configuration
@ComponentScan(basePackageClasses = Package.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class))
public class GridFsConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(GridFsConfiguration.class);

    @Value("${filesMongoUrls}")
    private String mongoUrls;
    @Value("${filesMongoDatabase}")
    private String mongoDatabase;
    @Value("${filesBucketName:fs}")
    private String bucketName;
    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    @Bean
    public ReactiveGridFsTemplate reactiveGridFsTemplate() {
        return new ReactiveGridFsTemplate(reactiveMongoDbFactory(), mappingMongoConverter, bucketName);
    }

    public MongoClient reactiveMongoClient() {
        logger.info("creating mongoClient using {}", mongoUrls);
        return MongoClients.create(String.format("mongodb://%s", mongoUrls));
    }

    protected String getDatabaseName() {
        return mongoDatabase;
    }
}
