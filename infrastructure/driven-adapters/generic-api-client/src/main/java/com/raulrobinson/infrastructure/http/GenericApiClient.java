package com.raulrobinson.infrastructure.http;

import com.raulrobinson.infrastructure.http.dto.ApiRequest;
import com.raulrobinson.infrastructure.http.dto.ApiResponse;
import reactor.core.publisher.Mono;

public interface GenericApiClient {
    <T> Mono<ApiResponse<T>> execute(ApiRequest request, Class<T> responseType);
}
