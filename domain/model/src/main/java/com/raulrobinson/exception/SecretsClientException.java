package com.raulrobinson.exception;

public class SecretsClientException extends RuntimeException {

    public SecretsClientException(String message) {
        super(message);
    }

    public SecretsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
