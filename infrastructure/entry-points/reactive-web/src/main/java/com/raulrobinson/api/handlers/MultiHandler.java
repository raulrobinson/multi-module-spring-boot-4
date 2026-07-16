package com.raulrobinson.api.handlers;

import com.raulrobinson.api.dto.MultiRequestDto;
import com.raulrobinson.api.mapper.MultiApiMapper;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.IMultiUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@RequiredArgsConstructor
public class MultiHandler extends RequestValidation {

    private final IMultiUseCase retrieve;
    private final MultiApiMapper mapper;

    public Mono<ServerResponse> process(ServerRequest request) {
        return validate(request)
                .flatMap(h -> request.bodyToMono(MultiRequestDto.class)
                        .switchIfEmpty(Mono.error(new RequestValidation.RequestValidationException(
                                400, "MISSING_BODY", "Request body is required")))
                        .flatMap(dto -> retrieve.execute(h.operation(), mapper.toModel(dto),
                                Context.of("authHeader", h.authHeader(),
                                        "correlationId", h.correlationId())))
                        .flatMap(result -> ServerResponse.ok()
                                .header("X-Correlation-Id", h.correlationId())
                                .bodyValue(result)));
    }
}
