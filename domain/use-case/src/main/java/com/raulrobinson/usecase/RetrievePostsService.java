package com.raulrobinson.usecase;

import com.raulrobinson.model.Post;
import com.raulrobinson.ports.in.RetrievePostsUseCase;
import com.raulrobinson.ports.out.PostsGateway;
import reactor.core.publisher.Flux;

public class RetrievePostsService implements RetrievePostsUseCase {

    private final PostsGateway postsGateway;

    public RetrievePostsService(PostsGateway postsGateway) {
        this.postsGateway = postsGateway;
    }

    @Override
    public Flux<Post> retrieveAll() {
        return postsGateway.retrieveAll();
    }
}
