package ru.roborox.crawler.persist.configuration;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfiguration extends AbstractReactiveMongoConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(MongoConfiguration.class);

    @Value("${mongoUrls}")
    private String mongoUrls;
    @Value("${mongoDatabase}")
    private String mongoDatabase;

    @Bean
    @Primary
    public MongoClient mongoClient() {
        logger.info("creating mongoClient using {}", mongoUrls);
        return MongoClients.create(String.format("mongodb://%s", mongoUrls));
    }

    @Bean
    @Primary
    public MongoDatabase database() {
        return mongoClient().getDatabase(mongoDatabase);
    }

    @Override
    public com.mongodb.reactivestreams.client.MongoClient reactiveMongoClient() {
        return mongoClient();
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabase;
    }
}
