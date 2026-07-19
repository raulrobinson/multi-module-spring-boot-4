package com.raulrobinson.infrastructure.http.dto;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record ApiRequest(
        String operation,
        Map<String, String> pathParams,
        Map<String, String> queryParams,
        Map<String, String> headers,
        Object body,
        String correlationId
) {
    public ApiRequest {
        operation = requireText(operation, "operation");
        pathParams = copy(pathParams);
        queryParams = copy(queryParams);
        headers = copy(headers);
        correlationId = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString() : correlationId;
    }

    private static Map<String, String> copy(Map<String, String> source) {
        return source == null ? Map.of() : Map.copyOf(source);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " es obligatorio");
        if (value.isBlank()) throw new IllegalArgumentException(field + " no puede estar vacío");
        return value;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String operation;
        private Map<String, String> pathParams = Map.of();
        private Map<String, String> queryParams = Map.of();
        private Map<String, String> headers = Map.of();
        private Object body;
        private String correlationId;

        public Builder operation(String value) { operation = value; return this; }
        public Builder pathParams(Map<String, String> value) { pathParams = value; return this; }
        public Builder queryParams(Map<String, String> value) { queryParams = value; return this; }
        public Builder headers(Map<String, String> value) { headers = value; return this; }
        public Builder body(Object value) { body = value; return this; }
        public Builder correlationId(String value) { correlationId = value; return this; }

        public ApiRequest build() {
            return new ApiRequest(operation, pathParams, queryParams, headers, body, correlationId);
        }
    }
}
