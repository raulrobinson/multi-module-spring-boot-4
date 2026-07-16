package com.raulrobinson.exception;

public class LambdaClientException extends RuntimeException {
    public LambdaClientException(String message) { super(message); }
    public LambdaClientException(String message, Throwable cause) { super(message, cause); }
}
