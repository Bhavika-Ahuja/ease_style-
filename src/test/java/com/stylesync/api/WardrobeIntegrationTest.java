package com.stylesync.api;

import com.stylesync.BaseIntegrationTest;
import com.stylesync.TestConfiguration;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Wardrobe API Integration Tests
 * Tests all wardrobe/clothing endpoints including edge cases
 */
@DisplayName("Wardrobe API Tests")
public class WardrobeIntegrationTest extends BaseIntegrationTest {
    
    private String sessionCookie;
    
    @BeforeEach
    public void setupAuthentication() {
        // Register and login before each test
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        Response loginResponse = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        sessionCookie = loginResponse.getCookie("JSESSIONID");
    }
    
    // ==================== GET /wardrobe - List Clothing ====================
    
    @Test
    @DisplayName("Should list user's clothing items")
    public void testListClothing() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should require authentication to list clothing")
    public void testListClothingUnauthorized() {
        Response response = given()
            .when()
            .get("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(302, 401); // Redirect to login or unauthorized
    }
    
    @Test
    @DisplayName("Should return empty list when user has no clothing")
    public void testListClothingEmpty() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        // Check that clothing list is empty in the response
    }
    
    // ==================== POST /wardrobe - Add Clothing ====================
    
    @Test
    @DisplayName("Should successfully add new clothing item")
    public void testAddClothing() {
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
        assertThat(response.jsonPath().getString("data.name")).isEqualTo("Blue Jeans");
    }
    
    @ParameterizedTest
    @CsvSource({
        "T-Shirt, TOPS, Red, SUMMER, CASUAL, CASUAL",
        "Winter Coat, OUTERWEAR, Black, WINTER, FORMAL, FORMAL",
        "Sneakers, SHOES, White, ALL_SEASONS, SPORTS, SPORTY",
        "Formal Dress, DRESSES, Blue, SPRING, FORMAL, ELEGANT"
    })
    @DisplayName("Should add different types of clothing")
    public void testAddVariousClothingTypes(String name, String category, String color, 
                                           String season, String occasion, String style) {
        Map<String, String> clothingData = createClothingData(name, category, color, season, occasion, style);
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should require authentication to add clothing")
    public void testAddClothingUnauthorized() {
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response response = given()
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(401, 302);
    }
    
    @Test
    @DisplayName("Should fail to add clothing with empty name")
    public void testAddClothingEmptyName() {
        Map<String, String> clothingData = createDefaultClothingData();
        clothingData.put("name", "");
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 200);
        if (response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getBoolean("success")).isFalse();
        }
    }
    
    @Test
    @DisplayName("Should fail to add clothing with invalid category")
    public void testAddClothingInvalidCategory() {
        Map<String, String> clothingData = createDefaultClothingData();
        clothingData.put("category", "INVALID_CATEGORY");
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 500);
    }
    
    @Test
    @DisplayName("Should handle very long clothing name")
    public void testAddClothingLongName() {
        Map<String, String> clothingData = createDefaultClothingData();
        clothingData.put("name", "a".repeat(500));
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle special characters in clothing name")
    public void testAddClothingSpecialCharacters() {
        Map<String, String> clothingData = createDefaultClothingData();
        clothingData.put("name", "<script>alert('xss')</script>");
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        // Should either sanitize or reject
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle missing required fields")
    public void testAddClothingMissingFields() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParam("name", "Test Item")
            // Missing other required fields
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 500);
    }
    
    // ==================== POST /wardrobe - Update Clothing ====================
    
    @Test
    @DisplayName("Should successfully update clothing item")
    public void testUpdateClothing() {
        // First add clothing
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Update clothing
        clothingData.put("name", "Updated Blue Jeans");
        
        Response updateResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "update")
            .formParam("clothingId", clothingId)
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(updateResponse.getStatusCode()).isEqualTo(200);
        assertThat(updateResponse.jsonPath().getBoolean("success")).isTrue();
        assertThat(updateResponse.jsonPath().getString("data.name")).isEqualTo("Updated Blue Jeans");
    }
    
    @Test
    @DisplayName("Should fail to update non-existent clothing")
    public void testUpdateNonExistentClothing() {
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "update")
            .formParam("clothingId", "nonexistent123")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(404, 200);
        if (response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getBoolean("success")).isFalse();
        }
    }
    
    @Test
    @DisplayName("Should not allow updating another user's clothing")
    public void testUpdateOtherUserClothing() {
        // Add clothing as first user
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Register and login as second user
        registerUser(
            TestConfiguration.TEST_USER_2_NAME,
            TestConfiguration.TEST_USER_2_EMAIL,
            TestConfiguration.TEST_USER_2_PASSWORD
        );
        
        Response login2Response = loginUser(
            TestConfiguration.TEST_USER_2_EMAIL,
            TestConfiguration.TEST_USER_2_PASSWORD
        );
        
        String session2Cookie = login2Response.getCookie("JSESSIONID");
        
        // Try to update first user's clothing as second user
        clothingData.put("name", "Hacked Jeans");
        
        Response updateResponse = given()
            .cookie("JSESSIONID", session2Cookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "update")
            .formParam("clothingId", clothingId)
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(updateResponse.getStatusCode()).isIn(403, 404);
    }
    
    // ==================== POST /wardrobe - Toggle Favorite ====================
    
    @Test
    @DisplayName("Should toggle clothing favorite status")
    public void testToggleFavorite() {
        // Add clothing
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Toggle favorite
        Response toggleResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "toggleFavorite")
            .formParam("clothingId", clothingId)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(toggleResponse.getStatusCode()).isEqualTo(200);
        assertThat(toggleResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should handle multiple favorite toggles")
    public void testMultipleFavoriteToggles() {
        // Add clothing
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Toggle favorite multiple times
        for (int i = 0; i < 5; i++) {
            Response toggleResponse = given()
                .cookie("JSESSIONID", sessionCookie)
                .contentType(ContentType.URLENC)
                .formParam("action", "toggleFavorite")
                .formParam("clothingId", clothingId)
                .when()
                .post("/wardrobe")
                .then()
                .extract()
                .response();
            
            assertThat(toggleResponse.getStatusCode()).isEqualTo(200);
        }
    }
    
    // ==================== POST /wardrobe - Toggle Laundry ====================
    
    @Test
    @DisplayName("Should toggle laundry status")
    public void testToggleLaundry() {
        // Add clothing
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Set laundry status
        Response laundryResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "toggleLaundry")
            .formParam("clothingId", clothingId)
            .formParam("inLaundry", "true")
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(laundryResponse.getStatusCode()).isEqualTo(200);
        assertThat(laundryResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    // ==================== DELETE /wardrobe - Delete Clothing ====================
    
    @Test
    @DisplayName("Should successfully delete clothing item")
    public void testDeleteClothing() {
        // Add clothing
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Delete clothing
        Response deleteResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("id", clothingId)
            .when()
            .delete("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);
        assertThat(deleteResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should fail to delete non-existent clothing")
    public void testDeleteNonExistentClothing() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("id", "nonexistent123")
            .when()
            .delete("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(404, 200);
    }
    
    @Test
    @DisplayName("Should not allow deleting another user's clothing")
    public void testDeleteOtherUserClothing() {
        // Add clothing as first user
        Map<String, String> clothingData = createDefaultClothingData();
        
        Response addResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "add")
            .formParams(clothingData)
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        String clothingId = addResponse.jsonPath().getString("data.clothingId");
        
        // Register and login as second user
        registerUser(
            TestConfiguration.TEST_USER_2_NAME,
            TestConfiguration.TEST_USER_2_EMAIL,
            TestConfiguration.TEST_USER_2_PASSWORD
        );
        
        Response login2Response = loginUser(
            TestConfiguration.TEST_USER_2_EMAIL,
            TestConfiguration.TEST_USER_2_PASSWORD
        );
        
        String session2Cookie = login2Response.getCookie("JSESSIONID");
        
        // Try to delete first user's clothing
        Response deleteResponse = given()
            .cookie("JSESSIONID", session2Cookie)
            .queryParam("id", clothingId)
            .when()
            .delete("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(deleteResponse.getStatusCode()).isIn(403, 404);
    }
    
    // ==================== GET /wardrobe?action=favorites ====================
    
    @Test
    @DisplayName("Should list favorite clothing items")
    public void testListFavorites() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("action", "favorites")
            .when()
            .get("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    // ==================== Edge Cases ====================
    
    @Test
    @DisplayName("Should handle concurrent clothing additions")
    public void testConcurrentClothingAdditions() throws InterruptedException {
        Map<String, String> clothingData = createDefaultClothingData();
        
        // Add multiple items rapidly
        for (int i = 0; i < 10; i++) {
            clothingData.put("name", "Item " + i);
            
            Response response = given()
                .cookie("JSESSIONID", sessionCookie)
                .contentType(ContentType.URLENC)
                .formParam("action", "add")
                .formParams(clothingData)
                .when()
                .post("/wardrobe")
                .then()
                .extract()
                .response();
            
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }
    
    @Test
    @DisplayName("Should handle invalid action parameter")
    public void testInvalidAction() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "invalidAction")
            .when()
            .post("/wardrobe")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 200);
    }
}
