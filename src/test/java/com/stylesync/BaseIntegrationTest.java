package com.stylesync;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Base Integration Test - provides common setup for all integration tests
 * Uses Testcontainers for MongoDB and embedded Jetty for servlet testing
 */
@Testcontainers
public abstract class BaseIntegrationTest {
    
    @Container
    protected static MongoDBContainer mongoDBContainer;
    
    static {
        // Configure Docker environment for Windows
        System.setProperty("DOCKER_HOST", "npipe:////./pipe/docker_engine");
        
        mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017)
            .withReuse(false);
    }
    
    protected static String mongoConnectionString;
    
    @BeforeAll
    public static void setupAll() {
        // Configure MongoDB connection
        mongoConnectionString = mongoDBContainer.getReplicaSetUrl(TestConfiguration.MONGODB_DATABASE);
        System.setProperty("MONGODB_URI", mongoConnectionString);
        
        // Configure REST Assured
        RestAssured.baseURI = TestConfiguration.BASE_API_URL;
        RestAssured.port = TestConfiguration.TEST_PORT;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @BeforeEach
    public void setup() {
        // Clear session cookies before each test
        RestAssured.reset();
        RestAssured.baseURI = TestConfiguration.BASE_API_URL;
        RestAssured.port = TestConfiguration.TEST_PORT;
    }
    
    @AfterAll
    public static void tearDownAll() {
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }
    
    /**
     * Helper method to register a user
     */
    protected Response registerUser(String name, String email, String password, String confirmPassword) {
        return given()
            .contentType(ContentType.URLENC)
            .formParam("name", name)
            .formParam("email", email)
            .formParam("password", password)
            .formParam("confirmPassword", confirmPassword)
            .when()
            .post("/register")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Helper method to register a user with matching passwords
     */
    protected Response registerUser(String name, String email, String password) {
        return registerUser(name, email, password, password);
    }
    
    /**
     * Helper method to login a user
     */
    protected Response loginUser(String email, String password) {
        return given()
            .contentType(ContentType.URLENC)
            .formParam("email", email)
            .formParam("password", password)
            .when()
            .post("/login")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Helper method to logout
     */
    protected Response logout() {
        return given()
            .when()
            .get("/logout")
            .then()
            .extract()
            .response();
    }
    
    /**
     * Helper method to get authenticated request specification
     */
    protected RequestSpecification authenticatedRequest(String email, String password) {
        Response loginResponse = loginUser(email, password);
        String sessionCookie = loginResponse.getCookie("JSESSIONID");
        
        return given()
            .cookie("JSESSIONID", sessionCookie);
    }
    
    /**
     * Helper method to create test clothing data
     */
    protected Map<String, String> createClothingData(String name, String category, 
                                                     String color, String season, 
                                                     String occasion, String style) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("category", category);
        data.put("color", color);
        data.put("season", season);
        data.put("occasion", occasion);
        data.put("style", style);
        data.put("imageUrl", "https://example.com/image.jpg");
        return data;
    }
    
    /**
     * Helper method to create default test clothing
     */
    protected Map<String, String> createDefaultClothingData() {
        return createClothingData(
            "Blue Jeans",
            "PANTS",
            "Blue",
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
    }
    
    /**
     * Helper method to create test outfit data
     */
    protected Map<String, String> createOutfitData(String name, String clothingIds, 
                                                   String season, String occasion, String style) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("clothingIds", clothingIds);
        data.put("season", season);
        data.put("occasion", occasion);
        data.put("style", style);
        data.put("thumbnailUrl", "https://example.com/outfit.jpg");
        return data;
    }
    
    /**
     * Helper method to extract JSON field from response
     */
    protected String extractJsonField(Response response, String fieldPath) {
        return response.jsonPath().getString(fieldPath);
    }
    
    /**
     * Helper method to check if response contains error message
     */
    protected boolean hasError(Response response) {
        try {
            String success = response.jsonPath().getString("success");
            return success != null && "false".equals(success);
        } catch (Exception e) {
            return false;
        }
    }
}
