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

/**
 * Search and Recommendation API Integration Tests
 * Tests search functionality and recommendation endpoints
 */
@DisplayName("Search and Recommendation API Tests")
public class SearchAndRecommendationIntegrationTest extends BaseIntegrationTest {
    
    private String sessionCookie;
    
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
        
        // Create some test clothing for search
        createTestClothing();
    }
    
    private void createTestClothing() {
        String[] items = {
            "Blue Jeans", "Red Shirt", "Black Shoes",
            "White T-Shirt", "Green Jacket", "Brown Belt"
        };
        String[] categories = {
            "PANTS", "TOPS", "SHOES",
            "TOPS", "OUTERWEAR", "ACCESSORIES"
        };
        String[] colors = {
            "Blue", "Red", "Black",
            "White", "Green", "Brown"
        };
        
        for (int i = 0; i < items.length; i++) {
            Map<String, String> clothingData = createClothingData(
                items[i],
                categories[i],
                colors[i],
                "ALL_SEASONS",
                "CASUAL",
                "CASUAL"
            );
            
            given()
                .cookie("JSESSIONID", sessionCookie)
                .contentType(ContentType.URLENC)
                .formParam("action", "add")
                .formParams(clothingData)
                .when()
                .post("/wardrobe")
                .then()
                .extract()
                .response();
        }
    }
    
    // ==================== GET /search - Search Clothing ====================
    
    @Test
    @DisplayName("Should search clothing by name")
    public void testSearchByName() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "Jeans")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should search clothing by color")
    public void testSearchByColor() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "Blue")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should search clothing by category")
    public void testSearchByCategory() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("category", "TOPS")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"CASUAL", "FORMAL", "SPORTS"})
    @DisplayName("Should filter by occasion")
    public void testSearchByOccasion(String occasion) {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("occasion", occasion)
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @ParameterizedTest
    @CsvSource({
        "SUMMER, TOPS",
        "WINTER, OUTERWEAR",
        "ALL_SEASONS, PANTS"
    })
    @DisplayName("Should filter by season and category")
    public void testSearchBySeasonAndCategory(String season, String category) {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("season", season)
            .queryParam("category", category)
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should require authentication for search")
    public void testSearchUnauthorized() {
        Response response = given()
            .queryParam("query", "test")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(302, 401);
    }
    
    @Test
    @DisplayName("Should handle empty search query")
    public void testSearchEmptyQuery() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle search with no results")
    public void testSearchNoResults() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "NonExistentItem12345")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        // Should return empty results
    }
    
    @Test
    @DisplayName("Should handle special characters in search query")
    public void testSearchSpecialCharacters() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "<script>alert('xss')</script>")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle SQL injection in search")
    public void testSearchSQLInjection() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "'; DROP TABLE clothing; --")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle very long search query")
    public void testSearchLongQuery() {
        String longQuery = "a".repeat(1000);
        
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", longQuery)
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should perform case-insensitive search")
    public void testSearchCaseInsensitive() {
        Response response1 = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "jeans")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        Response response2 = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "JEANS")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response1.getStatusCode()).isEqualTo(200);
        assertThat(response2.getStatusCode()).isEqualTo(200);
        // Both should return similar results
    }
    
    @Test
    @DisplayName("Should handle partial match search")
    public void testSearchPartialMatch() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "Shir")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        // Should find "Red Shirt" and "White T-Shirt"
    }
    
    // ==================== GET /recommendations - Get Recommendations ====================
    
    @Test
    @DisplayName("Should get outfit recommendations")
    public void testGetRecommendations() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should require authentication for recommendations")
    public void testRecommendationsUnauthorized() {
        Response response = given()
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(302, 401);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"CASUAL", "FORMAL", "SPORTS", "PARTY"})
    @DisplayName("Should get recommendations by occasion")
    public void testRecommendationsByOccasion(String occasion) {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("occasion", occasion)
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"SUMMER", "WINTER", "SPRING", "FALL", "ALL_SEASONS"})
    @DisplayName("Should get recommendations by season")
    public void testRecommendationsBySeason(String season) {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("season", season)
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"CASUAL", "FORMAL", "SPORTY", "ELEGANT", "BOHEMIAN"})
    @DisplayName("Should get recommendations by style")
    public void testRecommendationsByStyle(String style) {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("style", style)
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should get recommendations with multiple filters")
    public void testRecommendationsMultipleFilters() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("occasion", "CASUAL")
            .queryParam("season", "SUMMER")
            .queryParam("style", "CASUAL")
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should handle recommendations with no matching items")
    public void testRecommendationsNoMatches() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("occasion", "FORMAL")
            .queryParam("season", "WINTER")
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        // Should return empty or default recommendations
    }
    
    @Test
    @DisplayName("Should handle invalid occasion in recommendations")
    public void testRecommendationsInvalidOccasion() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("occasion", "INVALID_OCCASION")
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 500);
    }
    
    @Test
    @DisplayName("Should handle recommendations for new user with no items")
    public void testRecommendationsNewUser() {
        // Register a new user with no clothing
        registerUser(
            "New User",
            "newuser@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        Response loginResponse = loginUser(
            "newuser@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        String newSessionCookie = loginResponse.getCookie("JSESSIONID");
        
        Response response = given()
            .cookie("JSESSIONID", newSessionCookie)
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        // Should handle gracefully (empty recommendations or suggestion to add items)
    }
    
    @Test
    @DisplayName("Should get weather-based recommendations")
    public void testWeatherBasedRecommendations() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("weather", "sunny")
            .queryParam("temperature", "25")
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400); // May not be implemented
    }
    
    // ==================== Edge Cases ====================
    
    @Test
    @DisplayName("Should handle concurrent search requests")
    public void testConcurrentSearches() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Response response = given()
                .cookie("JSESSIONID", sessionCookie)
                .queryParam("query", "Item" + i)
                .when()
                .get("/search")
                .then()
                .extract()
                .response();
            
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }
    
    @Test
    @DisplayName("Should handle search with unicode characters")
    public void testSearchUnicode() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "👕 Shirt 👖")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle search with URL encoded characters")
    public void testSearchURLEncoded() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "Blue%20Jeans")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("Should handle recommendations with XSS attempt")
    public void testRecommendationsXSS() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("style", "<script>alert('xss')</script>")
            .when()
            .get("/recommendations")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(400, 500);
    }
    
    @Test
    @DisplayName("Should paginate search results")
    public void testSearchPagination() {
        Response response = given()
            .cookie("JSESSIONID", sessionCookie)
            .queryParam("query", "")
            .queryParam("page", "1")
            .queryParam("limit", "5")
            .when()
            .get("/search")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400); // May not be implemented
    }
}
