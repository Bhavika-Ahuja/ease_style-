package com.stylesync.exception;

/**
 * Base exception class for StyleSync application
 * Demonstrates INHERITANCE - all custom exceptions extend this
 */
public class StyleSyncException extends Exception {
    private static final long serialVersionUID = 1L;

    public StyleSyncException(String message) {
        super(message);
    }

    public StyleSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
