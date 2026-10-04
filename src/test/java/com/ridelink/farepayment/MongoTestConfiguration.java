package com.ridelink.farepayment;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@TestConfiguration(proxyBeanMethods = false)
public class MongoTestConfiguration {

    @Bean(destroyMethod = "shutdown")
    MongoServer mongoServer() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("127.0.0.1", 0);
        return server;
    }

    @Bean(destroyMethod = "destroy")
    MongoDatabaseFactory mongoDatabaseFactory(MongoServer server) {
        return new SimpleMongoClientDatabaseFactory(server.getConnectionString() + "/ridelinkdb_test");
    }
}
