package com.raulrobinson.exception;

public class SsmBadRequestException extends RuntimeException {
    public SsmBadRequestException(String message) { super(message); }
    public SsmBadRequestException(String message, Throwable cause) { super(message, cause); }
}
