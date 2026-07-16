package com.raulrobinson.driven.corename.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Logging explícito del ciclo de vida de cada llamada saliente, con
 * payloads, headers y query params enmascarados.
 *
 * <p>Defensivo: un fallo de logging JAMÁS rompe la llamada de negocio
 * ni dispara retry/circuito (lección del CCE de HttpHeaders en Spring 7).</p>
 */
public final class AdapterLogger {

    private static final Logger log = LoggerFactory.getLogger("adapters.external-api");

    private final SensitiveDataMasker masker;
    private final boolean logPayloads;
    private final boolean logHeaders;

    public AdapterLogger(SensitiveDataMasker masker, boolean logPayloads, boolean logHeaders) {
        this.masker = masker;
        this.logPayloads = logPayloads;
        this.logHeaders = logHeaders;
    }

    public void logRequest(String correlationId, String operation, String method,
                           String uri, Map<String, String> queryParams,
                           HttpHeaders headers, Object body) {
        if (!log.isInfoEnabled()) return;
        try {
            String queryLog = queryParams == null || queryParams.isEmpty() ? ""
                    : "?" + queryParams.entrySet().stream()
                            .map(e -> e.getKey() + "=" + masker.maskParam(e.getKey(), e.getValue()))
                            .collect(Collectors.joining("&"));

            log.info("direction=OUTBOUND | phase=REQUEST | correlationId={} | operation={} | method={} | uri={}{}{}{}",
                    correlationId, operation, method, uri, queryLog,
                    logHeaders ? " | headers=" + masker.maskHeaders(headers) : "",
                    logPayloads && body != null ? " | body=" + masker.maskObject(body) : "");
        } catch (Exception e) {
            log.warn("phase=LOGGING_FAILURE | point=REQUEST | cause={}", e.toString());
        }
    }

    public void logResponse(String correlationId, String operation, int status,
                            long elapsedMs, Object body) {
        if (!log.isInfoEnabled()) return;
        try {
            log.info("direction=OUTBOUND | phase=RESPONSE | correlationId={} | operation={} | httpStatus={} | elapsedMs={}{}",
                    correlationId, operation, status, elapsedMs,
                    logPayloads && body != null ? " | body=" + masker.maskObject(body) : "");
        } catch (Exception e) {
            log.warn("phase=LOGGING_FAILURE | point=RESPONSE | cause={}", e.toString());
        }
    }

    public void logBusinessError(String correlationId, String operation, int status,
                                 long elapsedMs, String backendCode, String backendMessage) {
        log.warn("direction=OUTBOUND | phase=BUSINESS_ERROR | correlationId={} | operation={} | httpStatus={} | elapsedMs={} | backendCode={} | backendMessage={}",
                correlationId, operation, status, elapsedMs, backendCode, backendMessage);
    }

    public void logTechnicalError(String correlationId, String operation,
                                  long elapsedMs, String errorCode, Throwable cause) {
        log.error("direction=OUTBOUND | phase=TECHNICAL_ERROR | correlationId={} | operation={} | elapsedMs={} | errorCode={} | causeType={} | causeMessage={}",
                correlationId, operation, elapsedMs, errorCode,
                cause.getClass().getSimpleName(), cause.getMessage());
    }

    public void logRetryAttempt(String operation, int attempt, Throwable cause) {
        log.warn("direction=OUTBOUND | phase=RETRY | operation={} | attempt={} | causeType={}",
                operation, attempt, cause == null ? "N/A" : cause.getClass().getSimpleName());
    }

    public void logCircuitBreakerState(String name, String fromState, String toState) {
        log.warn("component=CIRCUIT_BREAKER | operation={} | transition={}->{}", name, fromState, toState);
    }
}
