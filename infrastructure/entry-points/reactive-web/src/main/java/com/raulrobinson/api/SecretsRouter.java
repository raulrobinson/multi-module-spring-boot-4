package com.raulrobinson.api;

import com.raulrobinson.api.handlers.SecretsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class SecretsRouter {

    private final SecretsHandler handler;

    @Bean
    public RouterFunction<ServerResponse> secretsRoutes() {
        return RouterFunctions.route()
                .POST("/api/secrets/list",  handler::listSecrets)
                .POST("/api/secrets/value", handler::getSecretValue)
                .build();
    }
}
