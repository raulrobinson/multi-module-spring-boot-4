package com.raulrobinson.exception;

/**
 * Fallo técnico: 5xx, timeout, conexión, circuito abierto, deserialization.
 * Participa en retry y en las métricas del circuit breaker (salvo EAI-T005,
 * excluido del retry por ser determinístico).
 */
public class TechnicalException extends RuntimeException {

    private final ErrorCatalog error;

    public TechnicalException(ErrorCatalog error, Throwable cause) {
        super("%s [%s]".formatted(error.message(), error.code()), cause);
        this.error = error;
    }

    public TechnicalException(ErrorCatalog error, String detail) {
        super("%s [%s] detail=%s".formatted(error.message(), error.code(), detail));
        this.error = error;
    }

    public ErrorCatalog error() { return error; }
}
