package com.raulrobinson.exception;

public class ApiGatewayAccessDeniedException extends RuntimeException {
    public ApiGatewayAccessDeniedException(String message) { super(message); }
    public ApiGatewayAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
