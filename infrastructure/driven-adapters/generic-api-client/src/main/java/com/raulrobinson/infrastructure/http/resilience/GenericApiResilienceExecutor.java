package com.raulrobinson.infrastructure.http.resilience;

import com.raulrobinson.infrastructure.http.error.ExternalCircuitOpenException;
import com.raulrobinson.infrastructure.http.error.ExternalClientException;
import com.raulrobinson.infrastructure.http.error.ExternalResponseMappingException;
import com.raulrobinson.infrastructure.http.error.ExternalTimeoutException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.concurrent.TimeoutException;
import reactor.core.publisher.Mono;

public final class GenericApiResilienceExecutor {
    private final CircuitBreakerRegistry circuitBreakers;
    private final RetryRegistry retries;
    private final TimeLimiterRegistry timeLimiters;

    public GenericApiResilienceExecutor(CircuitBreakerRegistry circuitBreakers,
                                        RetryRegistry retries,
                                        TimeLimiterRegistry timeLimiters) {
        this.circuitBreakers = circuitBreakers;
        this.retries = retries;
        this.timeLimiters = timeLimiters;
    }

    public <T> Mono<T> execute(String operation, Mono<T> invocation) {
        var timeout = timeLimiters.timeLimiter(operation);
        var retry = retries.retry(operation);
        var circuitBreaker = circuitBreakers.circuitBreaker(operation);

        return invocation
                .transformDeferred(TimeLimiterOperator.of(timeout))
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(TimeoutException.class,
                        error -> new ExternalTimeoutException(operation, error))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new ExternalCircuitOpenException(operation, error));
    }

    public static boolean retryable(Throwable error) {
        return !(error instanceof ExternalClientException)
                && !(error instanceof ExternalResponseMappingException)
                && !(error instanceof ExternalCircuitOpenException);
    }
}
