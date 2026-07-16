package com.raulrobinson.driven.corename.config;

import com.raulrobinson.driven.corename.logging.MaskingLevel;
import com.raulrobinson.model.HttpVerb;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Propiedades tipadas del adaptador, enlazadas 1:1 con el bloque
 * {@code adapters.external-api} del application.yml del servicio consumidor.
 *
 * <p>Cada operación apunta a su PROPIA API destino (URL absoluta) y define
 * su verbo y headers exclusivos. La resiliencia de instancia POR OPERACIÓN
 * con la configuración compartida de este bloque.</p>
 */
@Validated
@ConfigurationProperties(prefix = "adapters.external-api")
public record MultiExternalApiPropsConfig(

        // Definición por operación: URL absoluta, verbo y headers propios.
        @NotNull Map<String, Endpoint> endpoints,

        // Headers comunes a TODAS las operaciones (api keys van a Secrets, no aquí).
        Map<String, String> defaultHeaders,

        @NotNull Timeouts timeouts,
        @NotNull Resilience resilience,
        @NotNull Logging logging
) {

    // Operación contra una API destino.
    public record Endpoint(
            // URL absoluta. Ej: https://api-x.raulrobinson.com/cards/v1/retrieve-account
            @NotBlank String url,
            // Verbo HTTP de la operación; quien invoca no lo decide.
            @NotNull HttpVerb method,
            // Headers exclusivos de esta operación (P. ej.: Content-Type en POST).
            Map<String, String> headers
    ) {}

    // Timeouts de transporte (Netty) y de la operación completa.
    public record Timeouts(
            @NotNull Duration connect,
            @NotNull Duration read,
            @NotNull Duration write,
            // Timeout end-to-end por intento; lo aplica el TimeLimiter.
            @NotNull Duration operation
    ) {}

    // Config compartida de resiliencia; cada operación recibe su instancia aislada.
    public record Resilience(
            @NotNull CircuitBreaker circuitBreaker,
            @NotNull Retry retry
    ) {
        public record CircuitBreaker(
                float failureRateThreshold,
                float slowCallRateThreshold,
                @NotNull Duration slowCallDurationThreshold,
                int slidingWindowSize,
                int minimumNumberOfCalls,
                @NotNull Duration waitDurationInOpenState,
                int permittedNumberOfCallsInHalfOpenState
        ) {}

        public record Retry(
                int maxAttempts,
                @NotNull Duration waitDuration,
                double backoffMultiplier
        ) {}
    }

    // Logging explícito y enmascarado de datos sensibles.
    public record Logging(
            boolean logPayloads,
            boolean logHeaders,
            int maxPayloadLength,
            // Campos sensibles (body JSON, query params y headers; case-insensitive).
            @NotNull List<SensitiveField> sensitiveFields
    ) {
        public record SensitiveField(
                @NotBlank String name,
                @NotNull MaskingLevel level
        ) {}
    }
}

