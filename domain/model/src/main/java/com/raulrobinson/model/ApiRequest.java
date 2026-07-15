package com.raulrobinson.model;

import java.util.Map;

/**
 * Petición genérica hacia el backend externo. Contrato limpio: solo java.util.
 *
 * <p>El verbo HTTP NO se especifica aquí: lo define el YAML por operación
 * (adapters.external-api.endpoints.{operation}.method), eliminando la
 * posibilidad de invocar una operación con el verbo equivocado.</p>
 *
 * @param operation     clave del endpoint en adapters.external-api.endpoints
 * @param pathParams    reemplazos de {placeholders} en la URL
 * @param queryParams   query string
 * @param headers       headers dinámicos por invocación (ganan sobre default y endpoint)
 * @param body          payload; null para operaciones sin cuerpo
 * @param correlationId id de trazabilidad end-to-end
 */
public record ApiRequest(
        String operation,
        Map<String, String> pathParams,
        Map<String, String> queryParams,
        Map<String, String> headers,
        Object body,
        String correlationId
) {
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String operation;
        private Map<String, String> pathParams = Map.of();
        private Map<String, String> queryParams = Map.of();
        private Map<String, String> headers = Map.of();
        private Object body;
        private String correlationId;

        public Builder operation(String v) { this.operation = v; return this; }
        public Builder pathParams(Map<String, String> v) { this.pathParams = v; return this; }
        public Builder queryParams(Map<String, String> v) { this.queryParams = v; return this; }
        public Builder headers(Map<String, String> v) { this.headers = v; return this; }
        public Builder body(Object v) { this.body = v; return this; }
        public Builder correlationId(String v) { this.correlationId = v; return this; }

        public ApiRequest build() {
            return new ApiRequest(operation, pathParams, queryParams, headers, body,
                    correlationId != null ? correlationId : java.util.UUID.randomUUID().toString());
        }
    }
}
