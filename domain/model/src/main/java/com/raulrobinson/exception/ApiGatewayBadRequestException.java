package com.raulrobinson.exception;

public class ApiGatewayBadRequestException extends RuntimeException {
    public ApiGatewayBadRequestException(String message) { super(message); }
    public ApiGatewayBadRequestException(String message, Throwable cause) { super(message, cause); }
}
