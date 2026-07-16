package com.raulrobinson.driven.corename.config;

import com.raulrobinson.driven.corename.adapter.MultiExternalApiAdapter;
import com.raulrobinson.driven.corename.logging.AdapterLogger;
import com.raulrobinson.driven.corename.logging.SensitiveDataMasker;
import com.raulrobinson.driven.corename.metrics.AdapterMetrics;
import com.raulrobinson.exception.BusinessException;
import com.raulrobinson.ports.out.MultiExternalApiGateway;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.TimeUnit;

/**
 * Configuración autocontenida del módulo (Boot 4 / Spring 7 / Jackson 3).
 * El microservicio consumidor la importa y declara el bloque YAML.
 *
 * <p>Los registries de Resilience4j se comparten con el adaptador para
 * instance resiliencia Aislada POR OPERACIÓN (una instancia de CB/Retry/TL
 * por cada API destino, creada lazy con la config compartida del YAML).</p>
 */
@Configuration
@EnableConfigurationProperties(MultiExternalApiPropsConfig.class)
public class MultiExternalApiAdapterConfig {

    @Bean
    MultiExternalApiGateway externalApiGateway(MultiExternalApiPropsConfig props,
                                               JsonMapper mapper,
                                               MeterRegistry meterRegistry) {
        var masker = new SensitiveDataMasker(mapper, props.logging());
        var logger = new AdapterLogger(masker,
                props.logging().logPayloads(), props.logging().logHeaders());

        var cbRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig(props.resilience().circuitBreaker()));
        var retryRegistry = RetryRegistry.of(retryConfig(props.resilience().retry()));
        var tlRegistry = TimeLimiterRegistry.of(TimeLimiterConfig.custom()
                .timeoutDuration(props.timeouts().operation())
                .cancelRunningFuture(true)
                .build());

        // Métricas nativas de Resilience4j: cada instancia por operación
        // aparece automáticamente con su tag name=<operation>.
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(cbRegistry).bindTo(meterRegistry);
        TaggedRetryMetrics.ofRetryRegistry(retryRegistry).bindTo(meterRegistry);
        TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(tlRegistry).bindTo(meterRegistry);

        return new MultiExternalApiAdapter(
                buildWebClient(props),
                props,
                cbRegistry,
                retryRegistry,
                tlRegistry,
                logger,
                mapper,
                new AdapterMetrics(meterRegistry));
    }

    // ------------------------------------------------------------------
    // WebClient sin baseUrl (cada operación trae su URL absoluta).
    // El pool de conexiones de Reactor Netty ya separa por host.
    // ------------------------------------------------------------------

    private WebClient buildWebClient(MultiExternalApiPropsConfig props) {
        var t = props.timeouts();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) t.connect().toMillis())
                .responseTimeout(t.read())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(t.read().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(t.write().toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    // ------------------------------------------------------------------
    // Configs compartidas: los registries las aplican a cada operación.
    // ------------------------------------------------------------------

    private CircuitBreakerConfig circuitBreakerConfig(MultiExternalApiPropsConfig.Resilience.CircuitBreaker cfg) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(cfg.failureRateThreshold())
                .slowCallRateThreshold(cfg.slowCallRateThreshold())
                .slowCallDurationThreshold(cfg.slowCallDurationThreshold())
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(cfg.slidingWindowSize())
                .minimumNumberOfCalls(cfg.minimumNumberOfCalls())
                .waitDurationInOpenState(cfg.waitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(cfg.permittedNumberOfCallsInHalfOpenState())
                // Rechazo de negocio o bug de deserialization NO significan backend caído.
                .ignoreExceptions(BusinessException.class,
                        org.springframework.core.codec.CodecException.class)
                .build();
    }

    private RetryConfig retryConfig(MultiExternalApiPropsConfig.Resilience.Retry cfg) {
        return RetryConfig.custom()
                .maxAttempts(cfg.maxAttempts())
                .intervalFunction(IntervalFunction
                        .ofExponentialBackoff(cfg.waitDuration(), cfg.backoffMultiplier()))
                // Nunca reintentar: negocio (semántica) ni codec (determinístico —
                // reintentar una deserialization fallida quema llamadas para fallar igual).
                .ignoreExceptions(BusinessException.class,
                        org.springframework.core.codec.CodecException.class)
                .build();
    }
}

