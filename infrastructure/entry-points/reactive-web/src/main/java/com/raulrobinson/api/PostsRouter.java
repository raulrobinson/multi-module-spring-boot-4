package com.raulrobinson.api;

import com.raulrobinson.api.handlers.RetrievePostsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class PostsRouter {

    @Bean
    RouterFunction<ServerResponse> postsRoutes(
            RetrievePostsHandler handler
    ) {
        return route()
                .GET("/api/v1/posts", handler::retrieveAll)
                .build();
    }
}
