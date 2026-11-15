package com.stylesync.util;

import com.stylesync.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 * Ensures data integrity and security
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");
    
    /**
     * Validate email format
     */
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }
    
    /**
     * Validate password strength
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one letter and one number");
        }
    }
    
    /**
     * Validate required string field
     */
    public static void validateRequired(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
    
    /**
     * Validate string length
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) 
            throws ValidationException {
        if (value != null) {
            if (value.length() < minLength) {
                throw new ValidationException(fieldName + " must be at least " + minLength + " characters");
            }
            if (value.length() > maxLength) {
                throw new ValidationException(fieldName + " must not exceed " + maxLength + " characters");
            }
        }
    }
    
    /**
     * Sanitize string input to prevent XSS
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim()
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("/", "&#x2F;");
    }
}
