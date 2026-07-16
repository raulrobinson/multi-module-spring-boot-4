package com.raulrobinson.api;

import com.raulrobinson.api.handlers.DynamoDbHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class DynamoDbRouter {

    private final DynamoDbHandler handler;

    @Bean
    public RouterFunction<ServerResponse> dynamoDbRoutes() {
        return RouterFunctions.route()
                .POST("/api/dynamodb/tables",     handler::listTables)
                .POST("/api/dynamodb/table-info", handler::describeTable)
                .POST("/api/dynamodb/scan",       handler::scanTable)
                .build();
    }
}
