package ru.roborox.crawler.test.persist;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class AsyncMongoHelper {
    @Autowired
    private MongoDatabase database;

    private final List<String> ignoreCollections = Arrays.asList(
        "db_version",
        "counters"
    );

    public Flux<DeleteResult> cleanup() {
        return Flux.from(database.listCollectionNames())
            .filter(this::filterCollection)
            .flatMap(this::cleanupCollection);
    }

    private boolean filterCollection(String name) {
        return !ignoreCollections.contains(name) && !name.startsWith("system");
    }

    public Mono<DeleteResult> cleanupCollection(String collectionName) {
        return Mono.from(database.getCollection(collectionName).deleteMany(new Document()));
    }
}
