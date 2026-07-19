package com.raulrobinson.api.handlers;

import com.raulrobinson.ports.in.RetrievePostsUseCase;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class RetrievePostsHandler {

    private final RetrievePostsUseCase useCase;

    public RetrievePostsHandler(RetrievePostsUseCase useCase) {
        this.useCase = useCase;
    }

    public Mono<ServerResponse> retrieveAll(ServerRequest request) {
        return ServerResponse.ok()
                .body(useCase.retrieveAll(), Object.class);
    }
}
