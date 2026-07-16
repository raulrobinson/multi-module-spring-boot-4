package com.raulrobinson.exception;

public class SsoAccessDeniedException extends RuntimeException {
    public SsoAccessDeniedException(String message) { super(message); }
    public SsoAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
