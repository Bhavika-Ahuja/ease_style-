package com.stylesync.service;

import com.stylesync.exception.AuthenticationException;
import com.stylesync.exception.DatabaseException;
import com.stylesync.exception.ValidationException;
import com.stylesync.model.User;
import com.stylesync.repository.UserRepository;
import com.stylesync.util.PasswordUtil;
import com.stylesync.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * AuthenticationService - handles user authentication and registration
 * Demonstrates business logic layer and exception handling
 */
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    
    public AuthenticationService() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * Register a new user
     * @throws ValidationException if input is invalid
     * @throws AuthenticationException if email already exists
     */
    public User register(String name, String email, String password) 
            throws ValidationException, AuthenticationException, DatabaseException {
        
        logger.info("Attempting to register user: {}", email);
        
        // Validate inputs
        ValidationUtil.validateRequired(name, "Name");
        ValidationUtil.validateLength(name, "Name", 2, 100);
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePassword(password);
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("Email already registered");
        }
        
        // Hash password
        String passwordHash = PasswordUtil.hashPassword(password);
        
        // Create and save user
        User user = new User(name, email, passwordHash);
        user = userRepository.save(user);
        
        logger.info("User registered successfully: {}", email);
        return user;
    }
    
    /**
     * Login user
     * @throws ValidationException if input is invalid
     * @throws AuthenticationException if credentials are incorrect
     */
    public User login(String email, String password) 
            throws ValidationException, AuthenticationException, DatabaseException {
        
        logger.info("Login attempt for: {}", email);
        
        // Validate inputs
        ValidationUtil.validateRequired(email, "Email");
        ValidationUtil.validateRequired(password, "Password");
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid email or password");
        }
        
        User user = userOpt.get();
        
        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }
        
        // Update last login
        user.updateLastLogin();
        userRepository.updateLastLogin(user.getUserId());
        
        logger.info("User logged in successfully: {}", email);
        return user;
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(String userId) throws DatabaseException {
        return userRepository.findById(userId);
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) throws DatabaseException {
        return userRepository.findByEmail(email);
    }
}
