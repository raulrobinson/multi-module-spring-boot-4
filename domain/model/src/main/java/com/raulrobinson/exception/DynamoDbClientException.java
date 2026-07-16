package com.raulrobinson.exception;

public class DynamoDbClientException extends RuntimeException {
    public DynamoDbClientException(String message) { super(message); }
    public DynamoDbClientException(String message, Throwable cause) { super(message, cause); }
}
