package com.raulrobinson.ports.out;

import com.raulrobinson.model.ApiRequest;
import com.raulrobinson.model.ApiResponse;
import reactor.core.publisher.Mono;

/**
 * Puerto (driven port) que expone este módulo. Contrato limpio:
 * java.util + Reactor únicamente — apto para vivir en la capa de dominio.
 *
 * <pre>{@code
 * gateway.execute(ApiRequest.builder()
 *                 .operation("retrieve-account")
 *                 .queryParams(Map.of("accountNumber", acc))
 *                 .headers(Map.of("Authorization", "Bearer " + token))
 *                 .correlationId(cid)
 *                 .build(),
 *         RetrieveAccountResult.class)
 *        .map(ApiResponse::body);
 * }</pre>
 */
public interface MultiExternalApiGateway {

    /**
     * Ejecuta una operación contra su API destino aplicando resiliencia
     * Aislada POR OPERACIÓN (TimeLimiter → CircuitBreaker → Retry) y
     * deserialize la respuesta al tipo indicado dentro del adaptador.
     *
     * @param responseType tipo del dominio; Void.class si no interesa el cuerpo
     * @return error con BusinessException (4xx) o TechnicalException
     *         (5xx / timeout / circuito abierto / conexión / EAI-T005 si
     *         el body no mapea a responseType)
     */
    <T> Mono<ApiResponse<T>> execute(ApiRequest request, Class<T> responseType);
}
