package com.raulrobinson.api;

import com.raulrobinson.api.handlers.SsmHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class SsmRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final SsmHandler handler;

    @Bean
    public RouterFunction<ServerResponse> ssmRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/ssm/list",  handler::listParameters)
                .POST(basePath + "/" + version + "/ssm/value", handler::getParameterValue)
                .build();
    }
}
