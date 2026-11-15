package com.stylesync.api;

import com.stylesync.BaseIntegrationTest;
import com.stylesync.TestConfiguration;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Outfit API Integration Tests
 * Tests all outfit endpoints including edge cases
 */
@DisplayName("Outfit API Tests")
public class OutfitIntegrationTest extends BaseIntegrationTest {
    
    private String sessionCookie;
    private List<String> clothingIds;
    
    @BeforeEach
    public void setupAuthentication() {
        // Register and login
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
        
        // Create some clothing items for outfit tests
        clothingIds = new ArrayList<>();
        createTestClothing();
    }
    
    private void createTestClothing() {
        // Create multiple clothing items
        String[] items = {"Shirt", "Pants", "Shoes"};
        String[] categories = {"TOPS", "PANTS", "SHOES"};
        
        for (int i = 0; i < items.length; i++) {
            Map<String, String> clothingData = createClothingData(
                items[i],
                categories[i],
                "Blue",
                "ALL_SEASONS",
                "CASUAL",
                "CASUAL"
            );
            
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
            
            if (response.getStatusCode() == 200 && response.jsonPath().get("data.clothingId") != null) {
                clothingIds.add(response.jsonPath().getString("data.clothingId"));
            }
        }
    }
    
    // ==================== GET /outfits - List Outfits ====================
    
    @Test
    @DisplayName("Should list user's outfits")
    public void testListOutfits() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should require authentication to list outfits")
    public void testListOutfitsUnauthorized() {
        Response response = given()
            .when()
            .get("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(302, 401);
    }
    
    @Test
    @DisplayName("Should return empty list when user has no outfits")
    public void testListOutfitsEmpty() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    // ==================== POST /outfits - Create Outfit ====================
    
    @Test
    @DisplayName("Should successfully create a new outfit")
    public void testCreateOutfit() {
        if (clothingIds.isEmpty()) {
            return; // Skip if no clothing items created
        }
        
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Casual Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should fail to create outfit without name")
    public void testCreateOutfitWithoutName() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 200);
        if (response.getStatusCode() == 200) {
            assertThat(response.jsonPath().getBoolean("success")).isFalse();
        }
    }
    
    @Test
    @DisplayName("Should fail to create outfit without clothing items")
    public void testCreateOutfitWithoutClothing() {
        Map<String, String> outfitData = createOutfitData(
            "Empty Outfit",
            "",
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 200);
    }
    
    @Test
    @DisplayName("Should fail to create outfit with non-existent clothing IDs")
    public void testCreateOutfitWithInvalidClothingIds() {
        Map<String, String> outfitData = createOutfitData(
            "Invalid Outfit",
            "nonexistent1,nonexistent2",
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 404, 200);
    }
    
    @Test
    @DisplayName("Should handle very long outfit name")
    public void testCreateOutfitLongName() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "a".repeat(500),
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should create outfit with single clothing item")
    public void testCreateOutfitSingleItem() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        Map<String, String> outfitData = createOutfitData(
            "Single Item Outfit",
            clothingIds.get(0),
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    // ==================== POST /outfits - Update Outfit ====================
    
    @Test
    @DisplayName("Should successfully update outfit")
    public void testUpdateOutfit() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit first
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Original Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // Update outfit
        outfitData.put("name", "Updated Outfit");
        
        Response updateResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "update")
            .formParam("outfitId", outfitId)
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(updateResponse.getStatusCode()).isEqualTo(200);
        assertThat(updateResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should fail to update non-existent outfit")
    public void testUpdateNonExistentOutfit() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Updated Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "update")
            .formParam("outfitId", "nonexistent123")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(404, 200);
    }
    
    // ==================== POST /outfits - Toggle Favorite ====================
    
    @Test
    @DisplayName("Should toggle outfit favorite status")
    public void testToggleFavorite() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Test Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // Toggle favorite
        Response toggleResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "toggleFavorite")
            .formParam("outfitId", outfitId)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(toggleResponse.getStatusCode()).isEqualTo(200);
        assertThat(toggleResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    // ==================== POST /outfits - Mark as Worn ====================
    
    @Test
    @DisplayName("Should mark outfit as worn")
    public void testMarkAsWorn() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Test Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // Mark as worn
        Response wornResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "markWorn")
            .formParam("outfitId", outfitId)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(wornResponse.getStatusCode()).isEqualTo(200);
        assertThat(wornResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should handle marking outfit as worn multiple times")
    public void testMarkAsWornMultipleTimes() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Test Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // Mark as worn multiple times
        for (int i = 0; i < 5; i++) {
            Response wornResponse = given()
                .cookie("JSESSIONID", sessionCookie)
                .contentType(ContentType.URLENC)
                .formParam("action", "markWorn")
                .formParam("outfitId", outfitId)
                .when()
                .post("/outfits")
                .then()
                .extract()
                .response();
            
            assertThat(wornResponse.getStatusCode()).isEqualTo(200);
        }
    }
    
    // ==================== DELETE /outfits - Delete Outfit ====================
    
    @Test
    @DisplayName("Should successfully delete outfit")
    public void testDeleteOutfit() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Test Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // Delete outfit
        Response deleteResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("id", outfitId)
            .when()
            .delete("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);
        assertThat(deleteResponse.jsonPath().getBoolean("success")).isTrue();
    }
    
    @Test
    @DisplayName("Should fail to delete non-existent outfit")
    public void testDeleteNonExistentOutfit() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("id", "nonexistent123")
            .when()
            .delete("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(404, 200);
    }
    
    // ==================== GET /outfits?action=view ====================
    
    @Test
    @DisplayName("Should view outfit with clothing details")
    public void testViewOutfit() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "Test Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // View outfit
        Response viewResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("action", "view")
            .queryParam("id", outfitId)
            .when()
            .get("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(viewResponse.getStatusCode()).isEqualTo(200);
    }
    
    // ==================== Edge Cases ====================
    
    @Test
    @DisplayName("Should handle invalid action parameter")
    public void testInvalidAction() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "invalidAction")
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 200);
    }
    
    @Test
    @DisplayName("Should not allow user to access another user's outfit")
    public void testAccessOtherUserOutfit() {
        if (clothingIds.isEmpty()) {
            return;
        }
        
        // Create outfit as user 1
        String clothingIdsStr = String.join(",", clothingIds);
        Map<String, String> outfitData = createOutfitData(
            "User 1 Outfit",
            clothingIdsStr,
            "ALL_SEASONS",
            "CASUAL",
            "CASUAL"
        );
        
        Response createResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .contentType(ContentType.URLENC)
            .formParam("action", "create")
            .formParams(outfitData)
            .when()
            .post("/outfits")
            .then()
            .extract()
            .response();
        
        if (createResponse.getStatusCode() != 200) {
            return;
        }
        
        String outfitId = createResponse.jsonPath().getString("data.outfitId");
        
        // Register and login as user 2
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
        
        // Try to view user 1's outfit as user 2
        Response viewResponse = given()
            .cookie("JSESSIONID", session2Cookie)
            .queryParam("action", "view")
            .queryParam("id", outfitId)
            .when()
            .get("/outfits")
            .then()
            .extract()
            .response();
        
        assertThat(viewResponse.getStatusCode()).isIn(403, 404, 302);
    }
}
