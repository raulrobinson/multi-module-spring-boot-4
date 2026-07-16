package com.raulrobinson.exception;

public class SecretsTimeoutException extends RuntimeException {

    public SecretsTimeoutException(String message) {
        super(message);
    }

    public SecretsTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
