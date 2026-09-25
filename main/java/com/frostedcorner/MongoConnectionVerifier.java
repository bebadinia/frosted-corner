package com.frostedcorner;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MongoConnectionVerifier implements ApplicationRunner {

    private final MongoClient mongoClient;
    private final String databaseName;

    public MongoConnectionVerifier(MongoClient mongoClient,
                                   @Value("${spring.data.mongodb.database}") String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    @Override
    public void run(ApplicationArguments args) {
        mongoClient.getDatabase(databaseName).runCommand(new Document("ping", 1));
    }
}