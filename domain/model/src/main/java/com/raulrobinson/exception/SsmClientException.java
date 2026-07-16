package com.raulrobinson.exception;

public class SsmClientException extends RuntimeException {
    public SsmClientException(String message) { super(message); }
    public SsmClientException(String message, Throwable cause) { super(message, cause); }
}
