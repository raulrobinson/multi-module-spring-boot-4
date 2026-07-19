package com.raulrobinson.infrastructure.http.error;

public final class ExternalCircuitOpenException extends GenericApiClientException {
    public ExternalCircuitOpenException(String operation, Throwable cause) {
        super(operation, "El circuito del backend está abierto", cause);
    }
}
