package com.raulrobinson.exception;

public class EventBridgeAccessDeniedException extends RuntimeException {
    public EventBridgeAccessDeniedException(String message) { super(message); }
    public EventBridgeAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
