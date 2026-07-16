package com.raulrobinson.usecase;

import com.raulrobinson.model.ApiRequest;
import com.raulrobinson.model.MultiModel;
import com.raulrobinson.ports.in.IMultiUseCase;
import com.raulrobinson.ports.out.MultiExternalApiGateway;
import com.raulrobinson.usecase.mapper.MultiUseCaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class MultiUseCase implements IMultiUseCase {

    private final MultiExternalApiGateway gateway;

    @FunctionalInterface
    private interface OperationMapping {
        ApiRequest.Builder apply(MultiModel model, ApiRequest.Builder builder, Context ctx);
    }

    /**
     * Registro de operaciones: Instancia (no static) porque las entradas POST
     * referencian los mappers MapStruct inyectados. Las lambdas capturan ésto,
     * así que resuelven el mapper en la invocación — no usar method references
     * (mapper::toX) aquí: ligarían el receptor durante la construcción.
     */
    private final Map<String, OperationMapping> operations;

    public MultiUseCase(MultiExternalApiGateway gateway,
                        MultiUseCaseMapper multiUseCaseMapper) {
        this.gateway = gateway;
        this.operations = Map.of(
                // 🡻
                "encrypt-data", (m, b, ctx) -> b.body(multiUseCaseMapper.toEncryptModel(m)),
                // 🡻
                "decrypt-data", (m, b, ctx) -> b.body(multiUseCaseMapper.toDecryptModel(m))
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Mono<Map<String, Object>> execute(String operation, MultiModel model, Context ctx) {
        var operationMapping = operations.get(operation);
        if (operationMapping == null) {
            return Mono.error(new IllegalArgumentException(
                    "Operación no soportada: " + operation));
        }

        String correlationId = ctx.getOrDefault("correlationId", UUID.randomUUID().toString());

        var builder = ApiRequest.builder()
                .operation(operation)
                .headers(headersOf(ctx))
                .correlationId(correlationId);

        return gateway.execute(operationMapping.apply(model, builder, ctx).build(),
                        (Class<Map<String, Object>>) (Class) Map.class)
                .flatMap(resp -> Mono.justOrEmpty(resp.body()))
                .doOnNext(r -> log.info(
                        "useCase=multi-facade | phase=SUCCESS | operation={} | correlationId={}",
                        operation, correlationId));
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────────────────────────────────────

    // ------------------------------------------------------------------
    // Headers dinámicos: solo los que varían por request. Los fijos por
    // operación (Content-Type) viven en el YAML del endpoint.
    // Nunca enviar headers vacíos: son carnada para 400 "formato inválido".
    // ------------------------------------------------------------------

    private static Map<String, String> headersOf(Context ctx) {
        var headers = new LinkedHashMap<String, String>();
        putIfPresent(headers, "Authorization", ctx.getOrDefault("authHeader", ""));
        return headers;
    }

    // ------------------------------------------------------------------
    // Contratos de query por familia de operaciones
    // ------------------------------------------------------------------

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }
}
