package com.stylesync.exception;

/**
 * Exception thrown for database operation failures
 * Demonstrates INHERITANCE
 */
public class DatabaseException extends StyleSyncException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
