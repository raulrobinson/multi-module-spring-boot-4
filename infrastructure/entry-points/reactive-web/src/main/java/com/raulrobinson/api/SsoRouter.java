package com.raulrobinson.api;

import com.raulrobinson.api.handlers.SsoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class SsoRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final SsoHandler handler;

    @Bean
    public RouterFunction<ServerResponse> ssoRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/auth/sso/start", handler::start)
                .POST(basePath + "/" + version + "/auth/sso/poll", handler::poll)
                .POST(basePath + "/" + version + "/auth/sso/accounts", handler::accounts)
                .POST(basePath + "/" + version + "/auth/sso/roles", handler::roles)
                .POST(basePath + "/" + version + "/auth/sso/credentials", handler::credentials)
                .build();
    }
}
