package com.raulrobinson.exception;

public class EventBridgeBadRequestException extends RuntimeException {
    public EventBridgeBadRequestException(String message) { super(message); }
    public EventBridgeBadRequestException(String message, Throwable cause) { super(message, cause); }
}
