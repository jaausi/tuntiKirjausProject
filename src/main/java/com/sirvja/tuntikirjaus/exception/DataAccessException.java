package com.sirvja.tuntikirjaus.exception;

/**
 * Thrown when a database operation fails. Unchecked so that it propagates to the UI,
 * where it is shown to the user instead of being silently swallowed.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
