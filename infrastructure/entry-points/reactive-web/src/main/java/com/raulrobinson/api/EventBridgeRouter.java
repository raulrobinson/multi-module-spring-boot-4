package com.raulrobinson.api;

import com.raulrobinson.api.handlers.EventBridgeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class EventBridgeRouter {

    private final EventBridgeHandler handler;

    @Bean
    public RouterFunction<ServerResponse> eventBridgeRoutes() {
        return RouterFunctions.route()
                .POST("/api/eventbridge/buses",       handler::listBuses)
                .POST("/api/eventbridge/rules",       handler::listRules)
                .POST("/api/eventbridge/rule-detail", handler::getRuleDetail)
                .POST("/api/eventbridge/put-event",   handler::putEvent)
                .build();
    }
}
