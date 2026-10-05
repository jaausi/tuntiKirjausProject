package com.sirvja.tuntikirjaus.logging;

/**
 * Thrown when logs can't be sent. The message is in Finnish so it can be shown to the user as is.
 */
public class LogUploadException extends RuntimeException {

    public LogUploadException(String message) {
        super(message);
    }

    public LogUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
