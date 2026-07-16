package com.raulrobinson.api;

import com.raulrobinson.api.handlers.LambdaHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class LambdaRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final LambdaHandler handler;

    @Bean
    public RouterFunction<ServerResponse> lambdaRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/lambda/aws-send",          handler::invokeFunction)
                .POST(basePath + "/" + version + "/lambda/aws-list-lambdas",  handler::listFunctions)
                .build();
    }
}
