package com.frostedcorner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class FrostedCornerApplicationTest {

    @Test
    void pingsConfiguredDatabaseOnStartup() throws Exception {
        MongoClient mongoClient = org.mockito.Mockito.mock(MongoClient.class);
        MongoDatabase mongoDatabase = org.mockito.Mockito.mock(MongoDatabase.class);
        when(mongoClient.getDatabase("frosted-corner")).thenReturn(mongoDatabase);

        MongoConnectionVerifier verifier = new MongoConnectionVerifier(mongoClient, "frosted-corner");

        verifier.run(new DefaultApplicationArguments());

        verify(mongoDatabase).runCommand(new Document("ping", 1));
    }
}