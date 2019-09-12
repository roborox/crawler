package ru.roborox.crawler.gridfs.configuration;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackageClasses = Package.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class))
public class GridFsConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(GridFsConfiguration.class);

    @Value("${filesMongoUrls}")
    private String mongoUrls;
    @Value("${filesMongoDatabase}")
    private String mongoDatabase;
    @Value("${filesBucketName}")
    private String bucketName;

    public MongoClient mongoClient() {
        logger.info("creating gridfs mongoClient using {}", mongoUrls);
        return MongoClients.create(String.format("mongodb://%s", mongoUrls));
    }

    public MongoDatabase mongoDatabase() {
        logger.info("creating gridfs database {}", mongoDatabase);
        return mongoClient().getDatabase(mongoDatabase);
    }

    @Bean
    public GridFSBucket gridfsBucket() {
        return GridFSBuckets.create(mongoDatabase(), bucketName);
    }
}
