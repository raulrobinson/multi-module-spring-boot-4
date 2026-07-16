package com.raulrobinson.exception;

public class IamAccessDeniedException extends RuntimeException {
    public IamAccessDeniedException(String message) { super(message); }
    public IamAccessDeniedException(String message, Throwable cause) { super(message, cause); }
}
