package com.stylesync.controller;

import com.stylesync.exception.AuthenticationException;
import com.stylesync.exception.DatabaseException;
import com.stylesync.exception.ValidationException;
import com.stylesync.model.User;
import com.stylesync.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * LoginServlet - handles user login
 * Demonstrates Jakarta Servlet API 6.0 usage
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private AuthenticationService authService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthenticationService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check if already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // Forward to login page
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            // Attempt login
            User user = authService.login(email, password);
            
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getName());
            session.setMaxInactiveInterval(3600); // 1 hour
            
            logger.info("User logged in successfully: {}", email);
            
            // Redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (ValidationException | AuthenticationException e) {
            logger.warn("Login failed for {}: {}", email, e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            
        } catch (DatabaseException e) {
            logger.error("Database error during login", e);
            request.setAttribute("error", "System error. Please try again later.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}
