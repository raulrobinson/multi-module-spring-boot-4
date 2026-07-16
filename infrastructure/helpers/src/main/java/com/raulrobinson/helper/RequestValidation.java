package com.raulrobinson.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Pre-validación de headers de entrada.*/
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestValidation {

    /** Headers validados y normalizados, listos para construir el Context. */
    public record ValidatedHeaders(
            String authHeader,
            String operation,
            String sourceBank,
            String correlationId
    ) {}

    /** Falla con status + code para que el handler responda el HTTP correcto. */
    public static final class RequestValidationException extends RuntimeException {
        private final int status;
        private final String code;

        public RequestValidationException(int status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }
        public int status() { return status; }
        public String code() { return code; }
    }

    public Mono<ValidatedHeaders> validate(ServerRequest request) {
        String authHeader = request.headers().firstHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new RequestValidationException(401, "UNAUTHORIZED",
                    "Missing or invalid Authorization header"));
        }

        String applicationId = request.headers().firstHeader("Application-Id");
        if (applicationId == null || applicationId.isBlank()) {
            return Mono.error(new RequestValidationException(400, "MISSING_APPLICATION_ID",
                    "Application-Id header is required"));
        }

        String operation = request.headers().firstHeader("Operation");
        if (operation == null || operation.isBlank()) {
            return Mono.error(new RequestValidationException(400, "MISSING_OPERATION",
                    "Operation header is required"));
        }

        String sourceBank = request.headers().firstHeader("Source-Bank");
        if (sourceBank == null || sourceBank.isBlank()) {
            return Mono.error(new RequestValidationException(400, "MISSING_SOURCE_BANK",
                    "Source-Bank header is required"));
        }

        String correlationId = Optional.ofNullable(request.headers().firstHeader("X-Correlation-Id"))
                .orElse(UUID.randomUUID().toString());

        return Mono.just(new ValidatedHeaders(authHeader, operation, sourceBank, correlationId));
    }

    protected String header(ServerRequest req, String name) {
        return req.headers().firstHeader(name);
    }

    protected String blankFallback(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    protected Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("message", message));
    }

    protected Mono<ServerResponse> serverError(Throwable e) {
        log.error("Secrets handler error", e);
        String msg = e.getMessage() != null ? e.getMessage() : "Error interno";
        return ServerResponse.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("message", msg));
    }
}