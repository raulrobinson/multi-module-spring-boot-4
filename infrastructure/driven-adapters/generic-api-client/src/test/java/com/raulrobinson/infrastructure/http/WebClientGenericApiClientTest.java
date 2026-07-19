package com.raulrobinson.infrastructure.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.raulrobinson.infrastructure.http.config.GenericApiClientProperties;
import com.raulrobinson.infrastructure.http.dto.ApiRequest;
import com.raulrobinson.infrastructure.http.dto.HttpVerb;
import com.raulrobinson.infrastructure.http.error.ExternalClientException;
import com.raulrobinson.infrastructure.http.resilience.GenericApiResilienceExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

class WebClientGenericApiClientTest {
    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void executesConfiguredOperationAndMapsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"value\":\"OK\"}"));

        var client = client("retrieve", HttpVerb.GET);
        var request = ApiRequest.builder()
                .operation("retrieve")
                .queryParams(Map.of("id", "123"))
                .correlationId("cid-1")
                .build();

        StepVerifier.create(client.execute(request, TestResponse.class))
                .assertNext(response -> {
                    assertThat(response.status()).isEqualTo(200);
                    assertThat(response.body().value()).isEqualTo("OK");
                })
                .verifyComplete();

        var recorded = server.takeRequest();
        assertThat(recorded.getPath()).isEqualTo("/resource?id=123");
        assertThat(recorded.getHeader("Correlation-Id")).isEqualTo("cid-1");
    }

    @Test
    void mapsFourHundredResponseWithoutRetry() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"not found\"}"));

        var client = client("retrieve", HttpVerb.GET);

        StepVerifier.create(client.execute(
                        ApiRequest.builder().operation("retrieve").build(), TestResponse.class))
                .expectErrorMatches(error -> error instanceof ExternalClientException exception
                        && exception.status() == 404)
                .verify();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private GenericApiClient client(String operation, HttpVerb method) {
        var endpoint = new GenericApiClientProperties.Endpoint(
                server.url("/resource").toString(), method, Map.of("Accept", "application/json"));
        var properties = new GenericApiClientProperties(
                Map.of(operation, endpoint), Map.of(),
                new GenericApiClientProperties.Transport(
                        Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(2), 1_048_576),
                new GenericApiClientProperties.Resilience(
                        new GenericApiClientProperties.Resilience.CircuitBreaker(
                                50, 100, Duration.ofSeconds(1), 10, 5,
                                Duration.ofSeconds(10), 2),
                        new GenericApiClientProperties.Resilience.Retry(
                                2, Duration.ofMillis(10), 1.0),
                        Duration.ofSeconds(3)));

        var retries = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .retryOnException(GenericApiResilienceExecutor::retryable)
                .build());
        var resilience = new GenericApiResilienceExecutor(
                CircuitBreakerRegistry.ofDefaults(), retries,
                TimeLimiterRegistry.of(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3)).build()));

        return new WebClientGenericApiClient(
                WebClient.create(), properties, resilience, JsonMapper.builder().build());
    }

    private record TestResponse(String value) { }
}
