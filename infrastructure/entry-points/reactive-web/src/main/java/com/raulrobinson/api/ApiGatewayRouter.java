package com.raulrobinson.api;

import com.raulrobinson.api.handlers.ApiGatewayHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class ApiGatewayRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final ApiGatewayHandler handler;

    @Bean
    public RouterFunction<ServerResponse> apiGatewayRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/apis",      handler::listApis)
                .POST(basePath + "/" + version + "/stages",    handler::listStages)
                .POST(basePath + "/" + version + "/resources", handler::listResources)
                .POST(basePath + "/" + version + "/keys",      handler::listKeys)
                .build();
    }
}
