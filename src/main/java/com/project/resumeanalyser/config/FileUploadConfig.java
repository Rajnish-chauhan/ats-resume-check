package com.project.resumeanalyser.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class FileUploadConfig extends AbstractMongoClientConfiguration {

    // This grabs the string directly from your application.properties!
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "resumedb";
    }

    @Override
    public MongoClient mongoClient() {
        //Now it uses the secure variable instead of a hardcoded string
        return MongoClients.create(mongoUri);
    }
}