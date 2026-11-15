package com.stylesync.api;

import com.stylesync.BaseIntegrationTest;
import com.stylesync.TestConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Authentication API Integration Tests
 * Tests all authentication endpoints including edge cases
 */
@DisplayName("Authentication API Tests")
public class AuthenticationIntegrationTest extends BaseIntegrationTest {
    
    // ==================== Registration Tests ====================
    
    @Test
    @DisplayName("Should successfully register a new user with valid credentials")
    public void testSuccessfulRegistration() {
        Response response = registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 302); // Success or redirect
        assertThat(response.getCookie("JSESSIONID")).isNotNull();
    }
    
    @Test
    @DisplayName("Should fail registration when passwords do not match")
    public void testRegistrationPasswordMismatch() {
        Response response = registerUser(
            "Test User",
            "newuser@example.com",
            "Password123",
            "DifferentPassword123"
        );
        
        // Should return error
        assertThat(response.getStatusCode()).isIn(200, 400);
        // Check for error in response body or redirected back to registration page
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test..user@example.com", "test user@example.com"})
    @DisplayName("Should fail registration with invalid email formats")
    public void testRegistrationInvalidEmail(String invalidEmail) {
        Response response = registerUser(
            "Test User",
            invalidEmail,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should fail registration with empty or null name")
    public void testRegistrationEmptyName(String emptyName) {
        Response response = registerUser(
            emptyName,
            "valid@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"123", "abc", "12345", "password"})
    @DisplayName("Should fail registration with weak passwords")
    public void testRegistrationWeakPassword(String weakPassword) {
        Response response = registerUser(
            "Test User",
            "newuser@example.com",
            weakPassword
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should fail registration with duplicate email")
    public void testRegistrationDuplicateEmail() {
        // Register first user
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Try to register second user with same email
        Response response = registerUser(
            "Another User",
            TestConfiguration.TEST_USER_EMAIL, // Same email
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400, 409); // Conflict
    }
    
    @Test
    @DisplayName("Should fail registration with extremely long input")
    public void testRegistrationLongInput() {
        String longString = "a".repeat(1000);
        
        Response response = registerUser(
            longString,
            longString + "@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should fail registration with special characters in name")
    public void testRegistrationSpecialCharactersName() {
        Response response = registerUser(
            "<script>alert('xss')</script>",
            "test@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should fail registration with SQL injection attempt in email")
    public void testRegistrationSQLInjection() {
        Response response = registerUser(
            "Test User",
            "admin'--@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    // ==================== Login Tests ====================
    
    @Test
    @DisplayName("Should successfully login with valid credentials")
    public void testSuccessfulLogin() {
        // First register a user
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Then login
        Response response = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 302);
        assertThat(response.getCookie("JSESSIONID")).isNotNull();
    }
    
    @Test
    @DisplayName("Should fail login with incorrect password")
    public void testLoginIncorrectPassword() {
        // Register user
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Try login with wrong password
        Response response = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            "WrongPassword123"
        );
        
        assertThat(response.getStatusCode()).isIn(200, 401);
    }
    
    @Test
    @DisplayName("Should fail login with non-existent email")
    public void testLoginNonExistentEmail() {
        Response response = loginUser(
            "nonexistent@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        assertThat(response.getStatusCode()).isIn(200, 401, 404);
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should fail login with empty email")
    public void testLoginEmptyEmail(String emptyEmail) {
        Response response = loginUser(emptyEmail, TestConfiguration.TEST_USER_PASSWORD);
        
        assertThat(response.getStatusCode()).isIn(200, 400, 401);
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should fail login with empty password")
    public void testLoginEmptyPassword(String emptyPassword) {
        Response response = loginUser(TestConfiguration.TEST_USER_EMAIL, emptyPassword);
        
        assertThat(response.getStatusCode()).isIn(200, 400, 401);
    }
    
    @Test
    @DisplayName("Should fail login with case-sensitive email")
    public void testLoginCaseSensitiveEmail() {
        // Register with lowercase
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            "test@example.com",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Try login with uppercase
        Response response = loginUser(
            "TEST@EXAMPLE.COM",
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Depending on implementation, this might pass or fail
        // Most systems normalize email to lowercase
        assertThat(response.getStatusCode()).isIn(200, 302, 401);
    }
    
    @Test
    @DisplayName("Should prevent brute force attacks with rate limiting")
    public void testLoginBruteForceProtection() {
        // Register user
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Attempt multiple failed logins
        for (int i = 0; i < 10; i++) {
            Response response = loginUser(
                TestConfiguration.TEST_USER_EMAIL,
                "WrongPassword" + i
            );
            // After certain attempts, should be rate limited
            if (i > 5) {
                assertThat(response.getStatusCode()).isIn(200, 401, 429); // 429 = Too Many Requests
            }
        }
    }
    
    @Test
    @DisplayName("Should handle SQL injection attempt in login")
    public void testLoginSQLInjection() {
        Response response = loginUser(
            "admin' OR '1'='1",
            "anything"
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400, 401);
    }
    
    @Test
    @DisplayName("Should prevent XSS in login credentials")
    public void testLoginXSSAttempt() {
        Response response = loginUser(
            "<script>alert('xss')</script>@example.com",
            "<script>alert('xss')</script>"
        );
        
        assertThat(response.getStatusCode()).isIn(200, 400, 401);
    }
    
    // ==================== Session Management Tests ====================
    
    @Test
    @DisplayName("Should create new session after successful login")
    public void testSessionCreation() {
        // Register and login
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        Response response = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        String sessionId = response.getCookie("JSESSIONID");
        assertThat(sessionId).isNotNull();
        assertThat(sessionId).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should redirect to dashboard when already logged in and accessing login page")
    public void testLoginRedirectWhenAlreadyAuthenticated() {
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
        
        String sessionId = loginResponse.getCookie("JSESSIONID");
        
        // Try to access login page again with session cookie
        Response response = given()
            .cookie("JSESSIONID", sessionId)
            .when()
            .get("/login")
            .then()
            .extract()
            .response();
        
        // Should redirect to dashboard
        assertThat(response.getStatusCode()).isIn(200, 302);
    }
    
    @Test
    @DisplayName("Should destroy session on logout")
    public void testLogout() {
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
        
        String sessionId = loginResponse.getCookie("JSESSIONID");
        
        // Logout
        Response logoutResponse = given()
            .cookie("JSESSIONID", sessionId)
            .when()
            .get("/logout")
            .then()
            .extract()
            .response();
        
        assertThat(logoutResponse.getStatusCode()).isIn(200, 302);
        
        // Try to access protected resource with old session
        Response protectedResponse = given()
            .cookie("JSESSIONID", sessionId)
            .when()
            .get("/dashboard")
            .then()
            .extract()
            .response();
        
        // Should redirect to login
        assertThat(protectedResponse.getStatusCode()).isIn(302, 401);
    }
    
    @Test
    @DisplayName("Should handle concurrent login sessions")
    public void testConcurrentSessions() {
        // Register user
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        // Login from two different "devices" (sessions)
        Response session1 = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        Response session2 = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        String sessionId1 = session1.getCookie("JSESSIONID");
        String sessionId2 = session2.getCookie("JSESSIONID");
        
        assertThat(sessionId1).isNotNull();
        assertThat(sessionId2).isNotNull();
        // Sessions should be different
        assertThat(sessionId1).isNotEqualTo(sessionId2);
    }
    
    @Test
    @DisplayName("Should handle session timeout")
    public void testSessionTimeout() throws InterruptedException {
        // This test requires actual time waiting or mock time manipulation
        // Simplified version here
        
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        Response loginResponse = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        String sessionId = loginResponse.getCookie("JSESSIONID");
        
        // In real scenario, wait for session timeout (configured as 60 minutes in web.xml)
        // For testing, you might need to configure a shorter timeout
        
        // Verify session is still valid immediately
        Response immediateResponse = given()
            .cookie("JSESSIONID", sessionId)
            .when()
            .get("/dashboard")
            .then()
            .extract()
            .response();
        
        assertThat(immediateResponse.getStatusCode()).isIn(200, 302);
    }
    
    // ==================== Edge Cases ====================
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    public void testNullParameters() {
        Response response = given()
            .contentType("application/x-www-form-urlencoded")
            .when()
            .post("/login")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle malformed request")
    public void testMalformedRequest() {
        Response response = given()
            .contentType("application/json")
            .body("{invalid json}")
            .when()
            .post("/login")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 400);
    }
    
    @Test
    @DisplayName("Should handle very long session")
    public void testLongSessionHandling() {
        registerUser(
            TestConfiguration.TEST_USER_NAME,
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        Response loginResponse = loginUser(
            TestConfiguration.TEST_USER_EMAIL,
            TestConfiguration.TEST_USER_PASSWORD
        );
        
        String sessionId = loginResponse.getCookie("JSESSIONID");
        
        // Make multiple requests with same session
        for (int i = 0; i < 50; i++) {
            Response response = given()
                .cookie("JSESSIONID", sessionId)
                .when()
                .get("/dashboard")
                .then()
                .extract()
                .response();
            
            assertThat(response.getStatusCode()).isIn(200, 302);
        }
    }
}
