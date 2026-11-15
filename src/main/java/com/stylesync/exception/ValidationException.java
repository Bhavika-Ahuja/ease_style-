package com.stylesync.exception;

/**
 * Exception thrown for validation failures
 * Demonstrates INHERITANCE
 */
public class ValidationException extends StyleSyncException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
