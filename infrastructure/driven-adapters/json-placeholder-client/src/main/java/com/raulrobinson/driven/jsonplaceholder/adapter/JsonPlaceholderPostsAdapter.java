package com.raulrobinson.driven.jsonplaceholder.adapter;

import com.raulrobinson.driven.jsonplaceholder.dto.PostResponseDto;
import com.raulrobinson.driven.jsonplaceholder.mapper.PostResponseMapper;
import com.raulrobinson.infrastructure.http.GenericApiClient;
import com.raulrobinson.infrastructure.http.dto.ApiRequest;
import com.raulrobinson.infrastructure.http.dto.ApiResponse;
import com.raulrobinson.model.Post;
import com.raulrobinson.ports.out.PostsGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class JsonPlaceholderPostsAdapter implements PostsGateway {

    private static final String OPERATION = "json-placeholder-retrieve-posts";

    private final GenericApiClient genericApiClient;
    private final PostResponseMapper mapper;

    public JsonPlaceholderPostsAdapter(
            GenericApiClient genericApiClient,
            PostResponseMapper mapper
    ) {
        this.genericApiClient = genericApiClient;
        this.mapper = mapper;
    }

    @Override
    public Flux<Post> retrieveAll() {
        ApiRequest request = ApiRequest.builder()
                .operation(OPERATION)
                .build();

        return genericApiClient
                .execute(request, PostResponseDto[].class)
                .map(ApiResponse::body)
                .flatMapMany(this::toFlux);
    }

    private Flux<Post> toFlux(PostResponseDto[] response) {
        if (response == null || response.length == 0) {
            return Flux.empty();
        }

        return Flux.fromArray(response)
                .map(mapper::toDomain);
    }
}
