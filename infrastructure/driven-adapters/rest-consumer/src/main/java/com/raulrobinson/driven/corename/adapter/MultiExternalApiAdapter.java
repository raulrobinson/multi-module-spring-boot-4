package com.raulrobinson.driven.corename.adapter;

import com.raulrobinson.driven.corename.config.MultiExternalApiPropsConfig;
import com.raulrobinson.driven.corename.logging.AdapterLogger;
import com.raulrobinson.driven.corename.metrics.AdapterMetrics;
import com.raulrobinson.exception.BusinessException;
import com.raulrobinson.exception.ErrorCatalog;
import com.raulrobinson.exception.TechnicalException;
import com.raulrobinson.model.ApiRequest;
import com.raulrobinson.model.ApiResponse;
import com.raulrobinson.model.HttpVerb;
import com.raulrobinson.ports.out.MultiExternalApiGateway;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.NullNode;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Implementación REST del puerto {@link MultiExternalApiGateway} para MÚLTIPLES
 * API's destino: cada operación del YAML apunta a su propia URL absoluta y
 * recibe instancias de resiliencia Aisladas (una API caída abre SU circuito,
 * las demás siguen fluyendo).
 *
 * <p>Pipeline por invocación: TimeLimiter → CircuitBreaker(operation) →
 * Retry(operation). Clasificación de errores:</p>
 * <ul>
 *   <li>4xx → {@link BusinessException}: no reintenta, no abre circuito.</li>
 *   <li>5xx / timeout / conexión → {@link TechnicalException}: reintenta y alimenta el circuito.</li>
 *   <li>Codec/deserialization → EAI-T005: determinístico, excluido del retry.</li>
 *   <li>Circuito abierto → EAI-T003 inmediata, sin tocar la red.</li>
 * </ul>
 *
 * <p>Stack objetivo: Spring Boot 4 / Spring Framework 7 / Jackson 3
 * ({@code tools.jackson}) / Reactor Netty.</p>
 */
public class MultiExternalApiAdapter implements MultiExternalApiGateway {

    private final WebClient webClient;
    private final MultiExternalApiPropsConfig properties;
    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry tlRegistry;
    private final AdapterLogger logger;
    private final ObjectMapper mapper;
    private final AdapterMetrics metrics;

    public MultiExternalApiAdapter(WebClient webClient,
                                   MultiExternalApiPropsConfig properties,
                                   CircuitBreakerRegistry cbRegistry,
                                   RetryRegistry retryRegistry,
                                   TimeLimiterRegistry tlRegistry,
                                   AdapterLogger logger,
                                   ObjectMapper mapper,
                                   AdapterMetrics metrics) {
        this.webClient = webClient;
        this.properties = properties;
        this.cbRegistry = cbRegistry;
        this.retryRegistry = retryRegistry;
        this.tlRegistry = tlRegistry;
        this.logger = logger;
        this.mapper = mapper;
        this.metrics = metrics;

        // Listeners a nivel de REGISTRY: cubren cada instancia por operación
        // en el momento en que se crea (lazy, primera invocación).
        cbRegistry.getEventPublisher().onEntryAdded(added ->
                added.getAddedEntry().getEventPublisher().onStateTransition(t ->
                        logger.logCircuitBreakerState(t.getCircuitBreakerName(),
                                t.getStateTransition().getFromState().name(),
                                t.getStateTransition().getToState().name())));
        retryRegistry.getEventPublisher().onEntryAdded(added ->
                added.getAddedEntry().getEventPublisher().onRetry(e ->
                        logger.logRetryAttempt(e.getName(),
                                e.getNumberOfRetryAttempts(), e.getLastThrowable())));
    }

    @Override
    public <T> Mono<ApiResponse<T>> execute(ApiRequest request, Class<T> responseType) {

        // Instancias de resiliencia POR OPERACIÓN: el registry crea (una vez,
        // con la config compartida del YAML) y cachea; llamadas siguientes reutilizan.
        var circuitBreaker = cbRegistry.circuitBreaker(request.operation());
        var retry = retryRegistry.retry(request.operation());
        var timeLimiter = tlRegistry.timeLimiter(request.operation());

        return Mono.defer(() -> {
                    long start = System.currentTimeMillis();

                    return doHttpCall(request, start, responseType)
                            .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                            .onErrorMap(TimeoutException.class,
                                    ex -> new TechnicalException(ErrorCatalog.EAI_T002, ex));
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorMap(this::mapResidualErrors)
                .doOnError(TechnicalException.class, ex -> {
                    logger.logTechnicalError(request.correlationId(), request.operation(),
                            -1, ex.error().code(), ex.getCause() != null ? ex.getCause() : ex);
                    metrics.countError(request.operation(),
                            AdapterMetrics.Outcome.TECHNICAL_ERROR, ex.error().code());
                });
    }

    // ------------------------------------------------------------------
    // Llamada HTTP
    // ------------------------------------------------------------------

    private <T> Mono<ApiResponse<T>> doHttpCall(ApiRequest request, long start,
                                                Class<T> responseType) {
        var endpoint = resolveEndpoint(request);

        // URL absoluta por operación (cada endpoint es una API destino distinta).
        URI uri = UriComponentsBuilder.fromUriString(endpoint.url())
                .queryParams(toMultiValueMap(request.queryParams()))
                .buildAndExpand(request.pathParams())
                .toUri();

        return webClient.method(HttpMethod.valueOf(endpoint.method().name()))
                .uri(uri)
                .headers(h -> buildHeaders(h, request, endpoint))
                .body(request.body() == null
                        ? BodyInserters.empty()
                        : BodyInserters.fromValue(request.body()))
                .httpRequest(r -> logger.logRequest(request.correlationId(), request.operation(),
                        endpoint.method().name(), endpoint.url(),
                        request.queryParams(),
                        headersSnapshot(request, endpoint), request.body()))
                .exchangeToMono(response -> response.bodyToMono(JsonNode.class)
                        .defaultIfEmpty(NullNode.getInstance())
                        .flatMap(body -> {
                            int status = response.statusCode().value();
                            long elapsed = System.currentTimeMillis() - start;

                            if (response.statusCode().is2xxSuccessful()) {
                                logger.logResponse(request.correlationId(), request.operation(),
                                        status, elapsed, body);
                                metrics.recordLatency(request.operation(),
                                        AdapterMetrics.Outcome.SUCCESS, status, elapsed);
                                return Mono.just(new ApiResponse<>(status,
                                        headersOf(response),
                                        deserialize(body, responseType)));
                            }

                            if (response.statusCode().is4xxClientError()) {
                                return Mono.error(toBusinessException(request, status, elapsed, body));
                            }

                            // 5xx u otros → técnico (reintenta / abre SU circuito)
                            logger.logTechnicalError(request.correlationId(), request.operation(),
                                    elapsed, ErrorCatalog.EAI_T001.code(),
                                    new IllegalStateException("HTTP " + status));

                            return Mono.error(new TechnicalException(ErrorCatalog.EAI_T001,
                                    "httpStatus=" + status + " body=" + body));
                        }));
    }

    private MultiExternalApiPropsConfig.Endpoint resolveEndpoint(ApiRequest request) {
        var endpoint = Optional.ofNullable(properties.endpoints().get(request.operation()))
                .orElseThrow(() -> new TechnicalException(ErrorCatalog.EAI_T999,
                        "Operación no configurada en adapters.external-api.endpoints: "
                                + request.operation()));

        // Red de seguridad: un GET con body delata una operación mal invocada.
        if (endpoint.method() == HttpVerb.GET
                && request.body() != null) {
            logger.logTechnicalError(request.correlationId(), request.operation(), -1,
                    "GET_WITH_BODY", new IllegalArgumentException(
                            "Operación GET invocada con body; el body será ignorado por el backend"));
        }

        return endpoint;
    }

    /** Fusión en 3 capas: default → endpoint → request (los del request ganan). */
    private void buildHeaders(HttpHeaders headers, ApiRequest request,
                              MultiExternalApiPropsConfig.Endpoint endpoint) {
        if (properties.defaultHeaders() != null) {
            properties.defaultHeaders().forEach(headers::set);
        }

        if (endpoint.headers() != null) {
            endpoint.headers().forEach(headers::set);
        }

        request.headers().forEach(headers::set);
        headers.set("X-Correlation-Id", request.correlationId());
    }

    private HttpHeaders headersSnapshot(ApiRequest request,
                                        MultiExternalApiPropsConfig.Endpoint endpoint) {
        var h = new HttpHeaders();
        buildHeaders(h, request, endpoint);

        return h;
    }

    private MultiValueMap<String, String> toMultiValueMap(Map<String, String> params) {
        var mv = new LinkedMultiValueMap<String, String>();
        if (params != null) params.forEach(mv::add);

        return mv;
    }

    /** Copia los headers de respuesta a un Map java.util inmutable: el dominio no ve Spring. */
    private Map<String, List<String>> headersOf(ClientResponse response) {
        var map = new LinkedHashMap<String, List<String>>();

        response.headers().asHttpHeaders()
                .forEach((k, v) -> map.put(k, List.copyOf(v)));

        return Collections.unmodifiableMap(map);
    }

    /**
     * Deserialize el JsonNode interno al tipo del dominio. Jackson NUNCA
     * cruza el puerto: es un detalle de infraestructura de este adaptador.
     */
    private <T> T deserialize(JsonNode body, Class<T> responseType) {
        if (responseType == Void.class || body == null || body.isNull()) return null;

        try {
            return mapper.treeToValue(body, responseType);
        } catch (Exception e) {
            throw new TechnicalException(ErrorCatalog.EAI_T005, e);
        }
    }

    // ------------------------------------------------------------------
    // Clasificación de errores
    // ------------------------------------------------------------------

    private BusinessException toBusinessException(ApiRequest request, int status,
                                                  long elapsed, JsonNode body) {
        ErrorCatalog catalog = switch (status) {
            case 400 -> ErrorCatalog.EAI_B001;
            case 401, 403 -> ErrorCatalog.EAI_B003;
            case 404 -> ErrorCatalog.EAI_B002;
            case 409 -> ErrorCatalog.EAI_B004;
            case 422 -> ErrorCatalog.EAI_B005;
            default -> ErrorCatalog.EAI_B001;
        };
        String backendCode = textAt(body, "code", "errorCode", "error");
        String backendMessage = textAt(body, "message", "errorMessage", "detail");

        logger.logBusinessError(request.correlationId(), request.operation(),
                status, elapsed, backendCode, backendMessage);
        metrics.recordLatency(request.operation(),
                AdapterMetrics.Outcome.BUSINESS_ERROR, status, elapsed);
        metrics.countError(request.operation(),
                AdapterMetrics.Outcome.BUSINESS_ERROR, catalog.code());

        return new BusinessException(catalog, status, backendCode, backendMessage);
    }

    private String textAt(JsonNode body, String... candidates) {
        if (body == null || body.isNull()) return "N/A";

        for (String field : candidates) {
            JsonNode n = body.get(field);
            if (n != null && !n.isNull()) return n.asString();
        }

        return "N/A";
    }

    /**
     * Última línea de defensa: All lo que llegue sin clasificar se traduce
     * a excepciones del catálogo. El consumidor nunca vera excepciones crudas
     * de Netty/Reactor/Resilience4j/codec.
     */
    private Throwable mapResidualErrors(Throwable ex) {
        if (ex instanceof BusinessException || ex instanceof TechnicalException) return ex;
        if (ex instanceof CallNotPermittedException cnp)
            return new TechnicalException(ErrorCatalog.EAI_T003, cnp);
        if (ex instanceof TimeoutException te)
            return new TechnicalException(ErrorCatalog.EAI_T002, te);
        // CodecException cubre DecodingException/EncodingException (Spring 7);
        // determinístico: también está excluido del retry en la configuración.
        if (ex instanceof org.springframework.core.codec.CodecException ce)
            return new TechnicalException(ErrorCatalog.EAI_T005, ce);
        if (ex instanceof WebClientRequestException wcre)
            return new TechnicalException(ErrorCatalog.EAI_T004, wcre);
        if (ex instanceof WebClientResponseException wcrs)
            return new TechnicalException(ErrorCatalog.EAI_T001, wcrs);

        return new TechnicalException(ErrorCatalog.EAI_T999, ex);
    }
}
