package com.raulrobinson.api;

import com.raulrobinson.api.handlers.IamHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class IamRouter {

    private final IamHandler handler;

    @Bean
    public RouterFunction<ServerResponse> iamRoutes() {
        return RouterFunctions.route()
                .POST("/api/iam/users", handler::listUsers)
                .POST("/api/iam/roles", handler::listRoles)
                .build();
    }
}
