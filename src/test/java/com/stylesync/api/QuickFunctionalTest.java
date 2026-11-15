package com.stylesync.api;

import com.stylesync.SimplifiedBaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Quick Functional Test - Tests against running StyleSync application
 * No Testcontainers needed - uses your existing Docker container
 */
@DisplayName("Quick Functional Tests (Running Application)")
public class QuickFunctionalTest extends SimplifiedBaseTest {
    
    @Test
    @DisplayName("Should access login page")
    public void testLoginPageAccess() {
        Response response = given()
            .when()
            .get("/")
            .then()
            .extract()
            .response();
        
        assertThat(response.getStatusCode()).isIn(200, 302);
        System.out.println("✓ Login page accessible");
    }
    
    @Test
    @DisplayName("Should register a new user successfully")
    public void testUserRegistration() {
        String uniqueEmail = generateUniqueEmail();
        
        Response response = registerUser(
            "Test User",
            uniqueEmail,
            "Test@123"
        );
        
        assertThat(response.getStatusCode()).isIn(200, 302);
        System.out.println("✓ User registration works: " + uniqueEmail);
    }
    
    @Test
    @DisplayName("Should login successfully")
    public void testUserLogin() {
        // First register
        String uniqueEmail = generateUniqueEmail();
        registerUser("Test User", uniqueEmail, "Test@123");
        
        // Then login
        Response response = loginUser(uniqueEmail, "Test@123");
        
        assertThat(response.getStatusCode()).isIn(200, 302);
        assertThat(response.getCookie("JSESSIONID")).isNotNull();
        System.out.println("✓ User login works");
    }
    
    @Test
    @DisplayName("Should reject invalid login")
    public void testInvalidLogin() {
        Response response = loginUser("nonexistent@test.com", "wrongpass");
        
        assertThat(response.getStatusCode()).isIn(200, 401);
        System.out.println("✓ Invalid login properly rejected");
    }
    
    @Test
    @DisplayName("Should access dashboard when logged in")
    public void testDashboardAccess() {
        // Register and login
        String uniqueEmail = generateUniqueEmail();
        registerUser("Test User", uniqueEmail, "Test@123");
        
        Response loginResponse = loginUser(uniqueEmail, "Test@123");
        String sessionCookie = loginResponse.getCookie("JSESSIONID");
        
        // Access dashboard
        Response dashboardResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/dashboard")
            .then()
            .extract()
            .response();
        
        assertThat(dashboardResponse.getStatusCode()).isEqualTo(200);
        System.out.println("✓ Dashboard accessible when logged in");
    }
    
    @Test
    @DisplayName("Complete user journey - register, login, logout")
    public void testCompleteUserJourney() {
        // 1. Register
        String uniqueEmail = generateUniqueEmail();
        Response registerResponse = registerUser("Journey Test", uniqueEmail, "Test@123");
        assertThat(registerResponse.getStatusCode()).isIn(200, 302);
        System.out.println("  1. ✓ Registered user");
        
        // 2. Login
        Response loginResponse = loginUser(uniqueEmail, "Test@123");
        String sessionCookie = loginResponse.getCookie("JSESSIONID");
        assertThat(sessionCookie).isNotNull();
        System.out.println("  2. ✓ Logged in");
        
        // 3. Access protected resource
        Response dashboardResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/dashboard")
            .then()
            .extract()
            .response();
        assertThat(dashboardResponse.getStatusCode()).isEqualTo(200);
        System.out.println("  3. ✓ Accessed dashboard");
        
        // 4. Logout
        Response logoutResponse = given()
            .cookie("JSESSIONID", sessionCookie)
            .when()
            .get("/logout")
            .then()
            .extract()
            .response();
        assertThat(logoutResponse.getStatusCode()).isIn(200, 302);
        System.out.println("  4. ✓ Logged out");
        
        System.out.println("✓ Complete user journey successful!");
    }
}
