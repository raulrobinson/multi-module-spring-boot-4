package com.raulrobinson.api;

import com.raulrobinson.api.handlers.EventBridgeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class EventBridgeRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final EventBridgeHandler handler;

    @Bean
    public RouterFunction<ServerResponse> eventBridgeRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/eventbridge/buses",       handler::listBuses)
                .POST(basePath + "/" + version + "/eventbridge/rules",       handler::listRules)
                .POST(basePath + "/" + version + "/eventbridge/rule-detail", handler::getRuleDetail)
                .POST(basePath + "/" + version + "/eventbridge/put-event",   handler::putEvent)
                .build();
    }
}
