package com.raulrobinson.infrastructure.http;

import com.raulrobinson.infrastructure.http.config.GenericApiClientProperties;
import com.raulrobinson.infrastructure.http.dto.ApiRequest;
import com.raulrobinson.infrastructure.http.dto.ApiResponse;
import com.raulrobinson.infrastructure.http.error.ExternalClientException;
import com.raulrobinson.infrastructure.http.error.ExternalConnectionException;
import com.raulrobinson.infrastructure.http.error.ExternalResponseMappingException;
import com.raulrobinson.infrastructure.http.error.ExternalServerException;
import com.raulrobinson.infrastructure.http.error.OperationNotConfiguredException;
import com.raulrobinson.infrastructure.http.resilience.GenericApiResilienceExecutor;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

public final class WebClientGenericApiClient implements GenericApiClient {
    private final WebClient webClient;
    private final GenericApiClientProperties properties;
    private final GenericApiResilienceExecutor resilience;
    private final JsonMapper jsonMapper;

    public WebClientGenericApiClient(WebClient webClient,
                                     GenericApiClientProperties properties,
                                     GenericApiResilienceExecutor resilience,
                                     JsonMapper jsonMapper) {
        this.webClient = webClient;
        this.properties = properties;
        this.resilience = resilience;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public <T> Mono<ApiResponse<T>> execute(ApiRequest request, Class<T> responseType) {
        var endpoint = endpoint(request.operation());
        return resilience.execute(request.operation(), invoke(request, endpoint, responseType));
    }

    private <T> Mono<ApiResponse<T>> invoke(
            ApiRequest request,
            GenericApiClientProperties.Endpoint endpoint,
            Class<T> responseType
    ) {
        URI uri = UriComponentsBuilder.fromUriString(endpoint.url())
                .queryParams(toQueryParams(request.queryParams()))
                .buildAndExpand(request.pathParams())
                .toUri();

        var spec = webClient.method(HttpMethod.valueOf(endpoint.method().name()))
                .uri(uri)
                .headers(headers -> mergeHeaders(headers, endpoint, request));

        var requestSpec = request.body() == null
                ? spec.body(BodyInserters.empty())
                : spec.bodyValue(request.body());

        return requestSpec.exchangeToMono(response -> decode(response, request.operation(), responseType))
                .onErrorMap(WebClientRequestException.class,
                        error -> new ExternalConnectionException(request.operation(), error));
    }

    private <T> Mono<ApiResponse<T>> decode(
            ClientResponse response, String operation, Class<T> responseType
    ) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    int status = response.statusCode().value();
                    if (status >= 400 && status < 500) {
                        return Mono.error(new ExternalClientException(operation, status, body));
                    }
                    if (status >= 500) {
                        return Mono.error(new ExternalServerException(operation, status, body));
                    }
                    return Mono.just(new ApiResponse<>(status, responseHeaders(response),
                            deserialize(operation, body, responseType)));
                });
    }

    private <T> T deserialize(String operation, String body, Class<T> responseType) {
        if (responseType == Void.class || body == null || body.isBlank()) return null;
        try {
            return jsonMapper.readValue(body, responseType);
        } catch (Exception error) {
            throw new ExternalResponseMappingException(operation, responseType, error);
        }
    }

    private GenericApiClientProperties.Endpoint endpoint(String operation) {
        var endpoint = properties.endpoints().get(operation);
        if (endpoint == null) throw new OperationNotConfiguredException(operation);
        return endpoint;
    }

    private void mergeHeaders(HttpHeaders target,
                              GenericApiClientProperties.Endpoint endpoint,
                              ApiRequest request) {
        if (properties.defaultHeaders() != null) properties.defaultHeaders().forEach(target::set);
        if (endpoint.headers() != null) endpoint.headers().forEach(target::set);
        request.headers().forEach(target::set);
        target.set("Correlation-Id", request.correlationId());
    }

    private org.springframework.util.MultiValueMap<String, String> toQueryParams(
            Map<String, String> source) {
        var result = new org.springframework.util.LinkedMultiValueMap<String, String>();
        source.forEach(result::add);
        return result;
    }

    private Map<String, List<String>> responseHeaders(ClientResponse response) {
        var headers = new LinkedHashMap<String, List<String>>();
        response.headers().asHttpHeaders()
                .forEach((name, values) -> headers.put(name, List.copyOf(values)));
        return Map.copyOf(headers);
    }
}
