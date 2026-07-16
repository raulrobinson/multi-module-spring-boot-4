package com.raulrobinson.exception;

public class SsoClientException extends RuntimeException {
    public SsoClientException(String message) { super(message); }
    public SsoClientException(String message, Throwable cause) { super(message, cause); }
}
