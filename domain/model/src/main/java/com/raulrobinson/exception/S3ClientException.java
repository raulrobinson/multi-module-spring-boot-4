package com.raulrobinson.exception;

public class S3ClientException extends RuntimeException {
    public S3ClientException(String message) { super(message); }
    public S3ClientException(String message, Throwable cause) { super(message, cause); }
}
