package com.raulrobinson.exception;

public class EventBridgeClientException extends RuntimeException {
    public EventBridgeClientException(String message) { super(message); }
    public EventBridgeClientException(String message, Throwable cause) { super(message, cause); }

    public EventBridgeClientException(String errorInesperadoConsultandoAwsEventBridge, String message) {
        super(message);
    }
}
