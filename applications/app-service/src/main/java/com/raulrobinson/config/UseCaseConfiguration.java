package com.raulrobinson.config;

import com.raulrobinson.ports.in.RetrievePostsUseCase;
import com.raulrobinson.ports.out.PostsGateway;
import com.raulrobinson.usecase.RetrievePostsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfiguration {

    @Bean
    RetrievePostsUseCase retrievePostsUseCase(
            PostsGateway postsGateway
    ) {
        return new RetrievePostsService(postsGateway);
    }
}
