package com.raulrobinson.ports.in;

import com.raulrobinson.model.Post;
import reactor.core.publisher.Flux;

public interface RetrievePostsUseCase {

    Flux<Post> retrieveAll();
}
