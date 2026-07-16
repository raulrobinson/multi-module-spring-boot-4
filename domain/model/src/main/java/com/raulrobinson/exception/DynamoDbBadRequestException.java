package com.raulrobinson.exception;

public class DynamoDbBadRequestException extends RuntimeException {
    public DynamoDbBadRequestException(String message) { super(message); }
    public DynamoDbBadRequestException(String message, Throwable cause) { super(message, cause); }
}
