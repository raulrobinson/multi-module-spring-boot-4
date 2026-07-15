package com.raulrobinson.exception;

/**
 * El backend externo respondió (4xx) rechazando la operación por reglas de negocio.
 * NO dispara retry ni abre el circuit breaker: es una respuesta válida del sistema.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCatalog error;
    private final int httpStatus;
    private final String backendCode;
    private final String backendMessage;

    public BusinessException(ErrorCatalog error, int httpStatus,
                             String backendCode, String backendMessage) {
        super("%s [%s] httpStatus=%d backendCode=%s backendMessage=%s"
                .formatted(error.message(), error.code(), httpStatus, backendCode, backendMessage));
        this.error = error;
        this.httpStatus = httpStatus;
        this.backendCode = backendCode;
        this.backendMessage = backendMessage;
    }

    public ErrorCatalog error() { return error; }
    public int httpStatus() { return httpStatus; }
    public String backendCode() { return backendCode; }
    public String backendMessage() { return backendMessage; }
}
