package com.raulrobinson.api;

import com.raulrobinson.api.handlers.SsoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class SsoRouter {

    private final SsoHandler handler;

    @Bean
    public RouterFunction<ServerResponse> ssoRoutes() {
        return RouterFunctions.route()
                .POST("/api/auth/sso/start", handler::start)
                .POST("/api/auth/sso/poll", handler::poll)
                .POST("/api/auth/sso/accounts", handler::accounts)
                .POST("/api/auth/sso/roles", handler::roles)
                .POST("/api/auth/sso/credentials", handler::credentials)
                // HEAD prefetch from Next.js Link
                .HEAD("/**", req -> ServerResponse.ok().build())
                .build();
    }
}
