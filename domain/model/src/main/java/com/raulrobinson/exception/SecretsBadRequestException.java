package com.raulrobinson.exception;

public class SecretsBadRequestException extends RuntimeException {

    public SecretsBadRequestException(String message) {
        super(message);
    }

    public SecretsBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
