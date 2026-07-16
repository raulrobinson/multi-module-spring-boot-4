package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.LambdaMapper;
import com.raulrobinson.exception.LambdaBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.ILambdaUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LambdaHandler extends RequestValidation {

    private final ILambdaUseCase lambda;
    private final LambdaMapper mapper;

    // ── POST /api/aws-send ───────────────────────────────────────────────────
    public Mono<ServerResponse> invokeFunction(ServerRequest request) {
        String arn             = header(request, "x-lambda-arn");
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");

        if (arn == null || arn.isBlank()) {
            return Mono.error(new LambdaBadRequestException("Falta header x-lambda-arn"));
        }
        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new LambdaBadRequestException("Faltan credenciales AWS"));
        }

        // arn:aws:lambda:<region>:<account>:function:<name>
        String[] parts = arn.split(":");
        String region = parts.length > 3 ? parts[3] : "us-east-1";

        return request.bodyToMono(String.class)
                .defaultIfEmpty("{}")
                .flatMap(body -> lambda.invokeFunction(accessKeyId, secretAccessKey, sessionToken, region, arn, body)
                        .map(mapper::toInvokeResponse)
                        .flatMap(dto -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dto)));
    }

    // ── POST /api/aws-list-lambdas ───────────────────────────────────────────
    public Mono<ServerResponse> listFunctions(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new LambdaBadRequestException("Faltan credenciales AWS"));
        }

        return lambda.listFunctions(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toFunctionsResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
