package com.raulrobinson.infrastructure.http.config;

import com.raulrobinson.infrastructure.http.GenericApiClient;
import com.raulrobinson.infrastructure.http.WebClientGenericApiClient;
import com.raulrobinson.infrastructure.http.error.ExternalClientException;
import com.raulrobinson.infrastructure.http.error.ExternalResponseMappingException;
import com.raulrobinson.infrastructure.http.resilience.GenericApiResilienceExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GenericApiClientProperties.class)
public class GenericApiClientConfiguration {

    @Bean
    GenericApiClient genericApiClient(WebClient genericWebClient,
                                      GenericApiClientProperties properties,
                                      GenericApiResilienceExecutor resilience,
                                      JsonMapper jsonMapper) {
        return new WebClientGenericApiClient(genericWebClient, properties, resilience, jsonMapper);
    }

    @Bean
    WebClient genericWebClient(GenericApiClientProperties properties) {
        var transport = properties.transport();
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(transport.connectTimeout().toMillis()))
                .responseTimeout(transport.readTimeout())
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(
                                transport.readTimeout().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(
                                transport.writeTimeout().toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(transport.maxInMemorySize()))
                .build();
    }

    @Bean
    GenericApiResilienceExecutor genericApiResilienceExecutor(
            GenericApiClientProperties properties
    ) {
        var resilience = properties.resilience();
        var cb = resilience.circuitBreaker();
        var retry = resilience.retry();

        var circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(cb.failureRateThreshold())
                .slowCallRateThreshold(cb.slowCallRateThreshold())
                .slowCallDurationThreshold(cb.slowCallDurationThreshold())
                .slidingWindowType(
                        CircuitBreakerConfig.SlidingWindowType.COUNT_BASED
                )
                .slidingWindowSize(cb.slidingWindowSize())
                .minimumNumberOfCalls(cb.minimumNumberOfCalls())
                .waitDurationInOpenState(cb.waitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(
                        cb.permittedCallsInHalfOpenState()
                )
                .ignoreExceptions(
                        ExternalClientException.class,
                        ExternalResponseMappingException.class
                )
                .build();

        var retryConfig = RetryConfig.custom()
                .maxAttempts(retry.maxAttempts())
                .waitDuration(retry.waitDuration())
                .retryOnException(
                        GenericApiResilienceExecutor::retryable
                )
                .build();

        var timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(resilience.timeout())
                .cancelRunningFuture(true)
                .build();

        return new GenericApiResilienceExecutor(
                CircuitBreakerRegistry.of(circuitBreakerConfig),
                RetryRegistry.of(retryConfig),
                TimeLimiterRegistry.of(timeLimiterConfig)
        );
    }
}
