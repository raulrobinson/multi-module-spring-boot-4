package com.raulrobinson.api;

import com.raulrobinson.api.handlers.LambdaHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class LambdaRouter {

    private final LambdaHandler handler;

    @Bean
    public RouterFunction<ServerResponse> lambdaRoutes() {
        return RouterFunctions.route()
                .POST("/api/aws-send",          handler::invokeFunction)
                .POST("/api/aws-list-lambdas",  handler::listFunctions)
                .build();
    }
}
