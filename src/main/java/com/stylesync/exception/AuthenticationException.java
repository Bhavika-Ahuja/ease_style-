package com.stylesync.exception;

/**
 * Exception thrown for authentication failures
 * Demonstrates INHERITANCE
 */
public class AuthenticationException extends StyleSyncException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
