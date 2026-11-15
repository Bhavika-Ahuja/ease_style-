package com.stylesync;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Simplified Base Test - Uses existing MongoDB instead of Testcontainers
 * This version works with your existing StyleSync application running in Docker
 */
public abstract class SimplifiedBaseTest {
    
    // Use your existing StyleSync application running on port 8081
    protected static final String BASE_URL = "http://localhost:8081";
    
    @BeforeAll
    public static void setupAll() {
        // Configure REST Assured to use your running application
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        System.out.println("=================================================");
        System.out.println("Testing against running StyleSync application");
        System.out.println("URL: " + BASE_URL);
        System.out.println("=================================================");
    }
    
    @BeforeEach
    public void setup() {
        // Reset REST Assured before each test
        RestAssured.reset();
        RestAssured.baseURI = BASE_URL;
    }
    
    @AfterAll
    public static void tearDownAll() {
        System.out.println("=================================================");
        System.out.println("Tests completed");
        System.out.println("=================================================");
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
    
    /**
     * Generate unique email for testing
     */
    protected String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }
}
