package com.raulrobinson.exception;

public class IamBadRequestException extends RuntimeException {
    public IamBadRequestException(String message) { super(message); }
    public IamBadRequestException(String message, Throwable cause) { super(message, cause); }
}
