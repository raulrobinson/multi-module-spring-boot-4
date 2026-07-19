package com.raulrobinson.infrastructure.http.config;

import com.raulrobinson.infrastructure.http.dto.HttpVerb;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("adapters.generic-api-client")
public record GenericApiClientProperties(
        @NotEmpty Map<String, @Valid Endpoint> endpoints,
        Map<String, String> defaultHeaders,
        @Valid @NotNull Transport transport,
        @Valid @NotNull Resilience resilience
) {
    public record Endpoint(
            @NotBlank String url,
            @NotNull HttpVerb method,
            Map<String, String> headers
    ) { }

    public record Transport(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            @NotNull Duration writeTimeout,
            @Min(1024) int maxInMemorySize
    ) { }

    public record Resilience(
            @Valid @NotNull CircuitBreaker circuitBreaker,
            @Valid @NotNull Retry retry,
            @NotNull Duration timeout
    ) {
        public record CircuitBreaker(
                float failureRateThreshold,
                float slowCallRateThreshold,
                @NotNull Duration slowCallDurationThreshold,
                @Min(1) int slidingWindowSize,
                @Min(1) int minimumNumberOfCalls,
                @NotNull Duration waitDurationInOpenState,
                @Min(1) int permittedCallsInHalfOpenState
        ) { }

        public record Retry(
                @Min(1) int maxAttempts,
                @NotNull Duration waitDuration
        ) { }
    }
}
