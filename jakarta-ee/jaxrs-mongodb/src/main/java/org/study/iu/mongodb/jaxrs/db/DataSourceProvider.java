package org.study.iu.mongodb.jaxrs.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class DataSourceProvider {
    private static final String CONNECTION_URI = "mongodb://" + System.getenv("DB_HOST") + ":27017/";
    public static final String DB_NAME = "testdb";
    public static final String COLLECTION_NAME = "example_collection";

    public static MongoClient getMongoClient() {
        return MongoClients.create(CONNECTION_URI);
    }
}