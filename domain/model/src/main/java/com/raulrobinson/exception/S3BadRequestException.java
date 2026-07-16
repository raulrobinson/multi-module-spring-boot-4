package com.raulrobinson.exception;

public class S3BadRequestException extends RuntimeException {
    public S3BadRequestException(String message) { super(message); }
    public S3BadRequestException(String message, Throwable cause) { super(message, cause); }
}
