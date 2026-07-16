package com.raulrobinson.api;

import com.raulrobinson.api.handlers.IamHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class IamRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final IamHandler handler;

    @Bean
    public RouterFunction<ServerResponse> iamRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/iam/users", handler::listUsers)
                .POST(basePath + "/" + version + "/iam/roles", handler::listRoles)
                .build();
    }
}
