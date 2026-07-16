package com.raulrobinson.api.error;

import java.time.OffsetDateTime;
import java.util.List;

/** Envelope corporativo de error: meta de trazabilidad + lista de errores. */
public record ErrorResponse(Meta meta, List<ErrorDetail> errors) {

    public record Meta(
            OffsetDateTime timestamp,
            String correlationId,
            String path,
            int status
    ) {}

    public record ErrorDetail(
            String code,          // catálogo propio: EAI-B002, UNAUTHORIZED...
            String message,       // mensaje del catálogo, estable
            String detail,        // detalle variable (backendMessage, causa)
            String backendCode,   // código del sistema origen si aplica
            Boolean retryable     // hint para el consumidor
    ) {
        public static ErrorDetail of(String code, String message) {
            return new ErrorDetail(code, message, null, null, null);
        }
    }
}