package com.raulrobinson.exception;

public class ApiGatewayClientException extends RuntimeException {
    public ApiGatewayClientException(String message) { super(message); }
    public ApiGatewayClientException(String message, Throwable cause) { super(message, cause); }
}
