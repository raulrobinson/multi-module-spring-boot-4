package com.raulrobinson.driven.corename.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;

/**
 * Métricas Micrometer del adaptador (Prometheus / CloudWatch):
 * <ul>
 *   <li>{@code external_api.client.latency} (Timer p50/p95/p99) — tags: operation, outcome, status</li>
 *   <li>{@code external_api.client.errors} (Counter) — tags: operation, type, code</li>
 * </ul>
 * Las métricas nativas de Resilience4j (estado del circuito POR OPERACIÓN,
 * reintentos, timeouts) se enlazan desde la configuración vía Tagged*Metrics.
 */
public final class AdapterMetrics {

    public static final String LATENCY = "external_api.client.latency";
    public static final String ERRORS = "external_api.client.errors";

    public enum Outcome { SUCCESS, BUSINESS_ERROR, TECHNICAL_ERROR }

    private final MeterRegistry registry;

    public AdapterMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordLatency(String operation, Outcome outcome, int httpStatus, long elapsedMs) {
        Timer.builder(LATENCY)
                .description("Latencia end-to-end de llamadas a APIs externas")
                .tag("operation", operation)
                .tag("outcome", outcome.name())
                .tag("status", httpStatus > 0 ? String.valueOf(httpStatus) : "N/A")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(Duration.ofMillis(elapsedMs));
    }

    public void countError(String operation, Outcome type, String errorCode) {
        Counter.builder(ERRORS)
                .description("Errores del adaptador clasificados por catálogo")
                .tag("operation", operation)
                .tag("type", type.name())
                .tag("code", errorCode)
                .register(registry)
                .increment();
    }
}
