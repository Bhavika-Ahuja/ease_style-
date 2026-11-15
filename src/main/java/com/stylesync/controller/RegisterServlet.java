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
 * RegisterServlet - handles user registration
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
    private AuthenticationService authService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthenticationService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        try {
            // Validate password match
            if (!password.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match");
            }
            
            // Register user
            User user = authService.register(name, email, password);
            
            // Auto-login after registration
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getName());
            session.setMaxInactiveInterval(3600);
            
            logger.info("User registered and logged in: {}", email);
            
            // Redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (ValidationException | AuthenticationException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            
        } catch (DatabaseException e) {
            logger.error("Database error during registration", e);
            request.setAttribute("error", "System error. Please try again later.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}
