package com.raulrobinson.exception;

public class SsoBadRequestException extends RuntimeException {
    public SsoBadRequestException(String message) { super(message); }
    public SsoBadRequestException(String message, Throwable cause) { super(message, cause); }
}
