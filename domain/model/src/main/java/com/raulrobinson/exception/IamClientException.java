package com.raulrobinson.exception;

public class IamClientException extends RuntimeException {
    public IamClientException(String message) { super(message); }
    public IamClientException(String message, Throwable cause) { super(message, cause); }
}
