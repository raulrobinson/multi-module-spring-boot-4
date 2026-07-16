package com.raulrobinson.api.error;

import com.raulrobinson.exception.BusinessException;
import com.raulrobinson.exception.TechnicalException;
import com.raulrobinson.helper.RequestValidation.RequestValidationException;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Traductor global de excepciones al envelope corporativo meta/errors[].
 * Order(-2): se ejecuta antes del DefaultErrorWebExceptionHandler de Boot.
 * Centraliza lo que antes vivía repetido en onErrorResume de cada handler.
 */
@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final JsonMapper mapper;

    @Override
    public @Nonnull Mono<Void> handle(ServerWebExchange exchange, @Nonnull Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);   // respuesta ya en curso: no se puede reescribir
        }

        String correlationId = Optional
                .ofNullable(exchange.getRequest().getHeaders().getFirst("X-Correlation-Id"))
                .orElse(UUID.randomUUID().toString());

        Mapped m = map(ex);

        var body = new ErrorResponse(
                new ErrorResponse.Meta(
                        OffsetDateTime.now(),
                        correlationId,
                        exchange.getRequest().getPath().value(),
                        m.status().value()),
                List.of(m.detail()));

        if (m.status().is5xxServerError()) {
            log.error("phase=GLOBAL_ERROR | correlationId={} | path={} | code={} | causeType={} | causeMessage={}",
                    correlationId, exchange.getRequest().getPath().value(),
                    m.detail().code(), ex.getClass().getSimpleName(), ex.getMessage());
        } else {
            log.warn("phase=GLOBAL_ERROR | correlationId={} | path={} | code={}",
                    correlationId, exchange.getRequest().getPath().value(), m.detail().code());
        }

        var response = exchange.getResponse();
        response.setStatusCode(m.status());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("X-Correlation-Id", correlationId);

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(body);
        } catch (Exception serializationError) {
            bytes = ("{\"errors\":[{\"code\":\"SERIALIZATION_ERROR\",\"message\":\"" +
                    m.detail().code() + "\"}]}").getBytes(StandardCharsets.UTF_8);
        }
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private record Mapped(HttpStatus status, ErrorResponse.ErrorDetail detail) {}

    /** Traducción excepción → status + detalle. Un solo lugar para el servicio completo. */
    private Mapped map(Throwable ex) {
        return switch (ex) {
            case RequestValidationException ve -> new Mapped(
                    HttpStatus.valueOf(ve.status()),
                    new ErrorResponse.ErrorDetail(ve.code(), ve.getMessage(), null, null, null));

            case BusinessException be -> new Mapped(
                    HttpStatus.valueOf(be.httpStatus()),
                    new ErrorResponse.ErrorDetail(
                            be.error().code(),
                            be.error().message(),
                            be.backendMessage(),
                            be.backendCode(),
                            false));                       // negocio: nunca reintentar

            case TechnicalException te -> new Mapped(
                    switch (te.error()) {
                        case EAI_T002 -> HttpStatus.GATEWAY_TIMEOUT;        // 504
                        case EAI_T003 -> HttpStatus.SERVICE_UNAVAILABLE;    // 503
                        default -> HttpStatus.BAD_GATEWAY;                  // 502
                    },
                    new ErrorResponse.ErrorDetail(
                            te.error().code(),
                            te.error().message(),
                            null,                          // sin detalle interno hacia afuera
                            null,
                            switch (te.error()) {          // hint de reintento al consumidor
                                case EAI_T001, EAI_T002, EAI_T003, EAI_T004 -> true;
                                default -> false;          // T005/T999: determinísticos
                            }));

            case ServerWebInputException swie -> new Mapped(
                    HttpStatus.BAD_REQUEST,
                    new ErrorResponse.ErrorDetail("INVALID_REQUEST",
                            "Cuerpo o parámetros de la petición inválidos",
                            swie.getReason(), null, false));

            case ResponseStatusException rse -> new Mapped(
                    HttpStatus.valueOf(rse.getStatusCode().value()),
                    ErrorResponse.ErrorDetail.of("HTTP_" + rse.getStatusCode().value(),
                            Optional.ofNullable(rse.getReason()).orElse("Error de la petición")));

            default -> new Mapped(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ErrorDetail.of("INTERNAL_ERROR",
                            "Error interno del servicio"));  // jamás filtrar ex.getMessage() en 500
        };
    }
}