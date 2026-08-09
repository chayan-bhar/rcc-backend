package com.ngo.ngoapp.config;

import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    /**
     * Applies the MongoDB Stable API (v1) to the MongoClient, exactly as recommended
     * in the Atlas connection snippet. This is required for proper TLS handshake
     * and protocol negotiation with Atlas clusters.
     */
    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> {
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            builder.serverApi(serverApi);
        };
    }
}
