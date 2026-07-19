package com.raulrobinson.infrastructure.http.error;

public final class ExternalResponseMappingException extends GenericApiClientException {
    public ExternalResponseMappingException(String operation, Class<?> type, Throwable cause) {
        super(operation, "No fue posible deserializar la respuesta como " + type.getName(), cause);
    }
}
