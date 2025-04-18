package com.sirvja.tuntikirjaus.exception;

public class StartTimeNotAfterLastTuntikirjausException extends Exception {
    public StartTimeNotAfterLastTuntikirjausException(String message) {
        super(message);
    }
}
