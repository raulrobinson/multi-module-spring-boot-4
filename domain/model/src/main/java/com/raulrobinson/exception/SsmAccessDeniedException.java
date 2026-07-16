package com.raulrobinson.exception;

public class SsmAccessDeniedException extends RuntimeException {
    public SsmAccessDeniedException(String message) { super(message); }
    public SsmAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
