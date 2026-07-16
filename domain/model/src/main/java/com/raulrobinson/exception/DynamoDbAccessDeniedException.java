package com.raulrobinson.exception;

public class DynamoDbAccessDeniedException extends RuntimeException {
    public DynamoDbAccessDeniedException(String message) { super(message); }
    public DynamoDbAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
