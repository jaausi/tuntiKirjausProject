package com.sirvja.tuntikirjaus.exception;

public class FieldNotInitializedException extends Exception {
    public FieldNotInitializedException(String timeFieldNotInitialized) {
        super(timeFieldNotInitialized);
    }
}
