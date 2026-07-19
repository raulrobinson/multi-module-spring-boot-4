package com.raulrobinson.ports.out;

import com.raulrobinson.model.Post;
import reactor.core.publisher.Flux;

public interface PostsGateway {

    Flux<Post> retrieveAll();
}
