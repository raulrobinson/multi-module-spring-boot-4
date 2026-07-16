package com.raulrobinson.api;

import com.raulrobinson.api.handlers.DynamoDbHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class DynamoDbRouter {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final DynamoDbHandler handler;

    @Bean
    public RouterFunction<ServerResponse> dynamoDbRoutes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/dynamodb/tables",     handler::listTables)
                .POST(basePath + "/" + version + "/dynamodb/table-info", handler::describeTable)
                .POST(basePath + "/" + version + "/dynamodb/scan",       handler::scanTable)
                .build();
    }
}
