package com.raulrobinson.api;

import com.raulrobinson.api.handlers.SsmHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class SsmRouter {

    private final SsmHandler handler;

    @Bean
    public RouterFunction<ServerResponse> ssmRoutes() {
        return RouterFunctions.route()
                .POST("/api/ssm/list",  handler::listParameters)
                .POST("/api/ssm/value", handler::getParameterValue)
                .build();
    }
}
