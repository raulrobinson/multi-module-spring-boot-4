package com.raulrobinson.exception;

public class LambdaBadRequestException extends RuntimeException {
    public LambdaBadRequestException(String message) { super(message); }
    public LambdaBadRequestException(String message, Throwable cause) { super(message, cause); }
}
