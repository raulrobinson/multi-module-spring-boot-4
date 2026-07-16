package com.raulrobinson.exception;

public class S3AccessDeniedException extends RuntimeException {
    public S3AccessDeniedException(String message) { super(message); }
    public S3AccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
