package com.stylesync.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt
 * Provides secure password handling
 */
public class PasswordUtil {
    
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
