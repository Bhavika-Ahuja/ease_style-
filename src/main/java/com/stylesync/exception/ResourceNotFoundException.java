package com.stylesync.exception;

/**
 * Exception thrown when a requested resource is not found
 * Demonstrates INHERITANCE
 */
public class ResourceNotFoundException extends StyleSyncException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
