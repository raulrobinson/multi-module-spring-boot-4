package com.raulrobinson.api;

import com.raulrobinson.api.handler.MultiHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final MultiHandler multiHandler;

    @Bean
    RouterFunction<ServerResponse> testRoutes() {
        return route()
                .POST(basePath + version + "/process", multiHandler::process)
                .build();
    }
}
