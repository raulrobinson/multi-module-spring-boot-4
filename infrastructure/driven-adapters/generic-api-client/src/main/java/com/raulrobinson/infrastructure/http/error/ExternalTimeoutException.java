package com.raulrobinson.infrastructure.http.error;

public final class ExternalTimeoutException extends GenericApiClientException {
    public ExternalTimeoutException(String operation, Throwable cause) {
        super(operation, "El backend excedió el tiempo de espera", cause);
    }
}
