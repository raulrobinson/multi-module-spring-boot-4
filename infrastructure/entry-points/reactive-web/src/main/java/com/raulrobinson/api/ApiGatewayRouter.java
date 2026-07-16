package com.raulrobinson.api;

import com.raulrobinson.api.handlers.ApiGatewayHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class ApiGatewayRouter {

    private final ApiGatewayHandler handler;

    @Bean
    public RouterFunction<ServerResponse> apiGatewayRoutes() {
        return RouterFunctions.route()
                .POST("/api/apigw/apis",      handler::listApis)
                .POST("/api/apigw/stages",    handler::listStages)
                .POST("/api/apigw/resources", handler::listResources)
                .POST("/api/apigw/keys",      handler::listKeys)
                .build();
    }
}
