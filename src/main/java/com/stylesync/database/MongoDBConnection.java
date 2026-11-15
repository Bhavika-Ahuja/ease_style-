package com.stylesync.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB Connection Manager - Singleton Pattern
 * Manages database connection lifecycle
 */
public class MongoDBConnection {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);
    
    private static MongoDBConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    private static final String DEFAULT_DATABASE = "stylesync";
    
    // Private constructor for Singleton pattern
    private MongoDBConnection() {
        connect();
    }
    
    /**
     * Get singleton instance - SINGLETON PATTERN
     */
    public static synchronized MongoDBConnection getInstance() {
        if (instance == null) {
            instance = new MongoDBConnection();
        }
        return instance;
    }
    
    /**
     * Establish MongoDB connection
     */
    private void connect() {
        try {
            // Get connection string from environment variable
            String mongoUri = System.getenv("MONGODB_URI");
            String dbName = System.getenv("MONGODB_DATABASE");
            
            if (mongoUri == null || mongoUri.isEmpty()) {
                logger.error("MONGODB_URI environment variable not set");
                throw new IllegalStateException("MONGODB_URI environment variable is required");
            }
            
            if (dbName == null || dbName.isEmpty()) {
                dbName = DEFAULT_DATABASE;
                logger.warn("MONGODB_DATABASE not set, using default: {}", DEFAULT_DATABASE);
            }
            
            // Configure MongoDB client settings
            ConnectionString connectionString = new ConnectionString(mongoUri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToConnectionPoolSettings(builder -> 
                        builder.maxConnectionIdleTime(60, TimeUnit.SECONDS)
                              .maxSize(50)
                    )
                    .applyToSocketSettings(builder -> 
                        builder.connectTimeout(10, TimeUnit.SECONDS)
                              .readTimeout(10, TimeUnit.SECONDS)
                    )
                    .build();
            
            // Create MongoDB client
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(dbName);
            
            // Test connection
            database.listCollectionNames().first();
            logger.info("Successfully connected to MongoDB database: {}", dbName);
            
        } catch (Exception e) {
            logger.error("Failed to connect to MongoDB", e);
            throw new RuntimeException("MongoDB connection failed", e);
        }
    }
    
    /**
     * Get MongoDB database instance
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }
    
    /**
     * Get MongoDB client instance
     */
    public MongoClient getClient() {
        if (mongoClient == null) {
            connect();
        }
        return mongoClient;
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            if (database != null) {
                database.listCollectionNames().first();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }
    
    /**
     * Close MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed");
        }
    }
    
    /**
     * Reconnect to database
     */
    public synchronized void reconnect() {
        close();
        connect();
    }
}
