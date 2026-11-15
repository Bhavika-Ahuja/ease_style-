package com.stylesync;

/**
 * Test Configuration - centralized configuration for all tests
 */
public class TestConfiguration {
    
    // Test Server Configuration
    public static final String TEST_HOST = "http://localhost";
    public static final int TEST_PORT = 8888;
    public static final String BASE_URL = TEST_HOST + ":" + TEST_PORT;
    public static final String CONTEXT_PATH = "/stylesync";
    public static final String BASE_API_URL = BASE_URL + CONTEXT_PATH;
    
    // Test MongoDB Configuration
    public static final String MONGODB_DATABASE = "stylesync_test";
    public static final String MONGODB_USERS_COLLECTION = "users";
    public static final String MONGODB_CLOTHING_COLLECTION = "clothing";
    public static final String MONGODB_OUTFITS_COLLECTION = "outfits";
    public static final String MONGODB_WEAR_HISTORY_COLLECTION = "wear_history";
    
    // Docker Configuration
    public static final String DOCKER_HOST = "npipe:////./pipe/docker_engine";
    
    // Test User Credentials
    public static final String TEST_USER_NAME = "Test User";
    public static final String TEST_USER_EMAIL = "test@example.com";
    public static final String TEST_USER_PASSWORD = "Test@123";
    
    public static final String TEST_USER_2_NAME = "Test User 2";
    public static final String TEST_USER_2_EMAIL = "test2@example.com";
    public static final String TEST_USER_2_PASSWORD = "Test2@123";
    
    // Test Data
    public static final String INVALID_EMAIL = "invalid-email";
    public static final String WEAK_PASSWORD = "123";
    public static final String EMPTY_STRING = "";
    public static final String LONG_STRING = "a".repeat(256);
    
    // Test Timeouts
    public static final int REQUEST_TIMEOUT = 5000; // 5 seconds
    public static final int SERVER_START_TIMEOUT = 30000; // 30 seconds
}
