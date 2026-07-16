package com.raulrobinson.exception;

public class LambdaAccessDeniedException extends RuntimeException {
    public LambdaAccessDeniedException(String message) { super(message); }
    public LambdaAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
