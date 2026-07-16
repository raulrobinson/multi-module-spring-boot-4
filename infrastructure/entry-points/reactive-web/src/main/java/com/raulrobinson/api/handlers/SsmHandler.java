package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.SsmMapper;
import com.raulrobinson.exception.SsmBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.ISsmUseCase;
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
public class SsmHandler extends RequestValidation {

    private final ISsmUseCase ssm;
    private final SsmMapper mapper;

    // ── POST /business/v1/api/ssm/list ────────────────────────────────────────────────────
    public Mono<ServerResponse> listParameters(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new SsmBadRequestException("Faltan credenciales AWS"));
        }

        return ssm.listParameters(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toParametersResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /business/v1/api/ssm/value ───────────────────────────────────────────────────
    public Mono<ServerResponse> getParameterValue(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String parameterName   = header(request, "x-parameter-name");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new SsmBadRequestException("Faltan credenciales AWS"));
        }
        if (parameterName == null || parameterName.isBlank()) {
            return Mono.error(new SsmBadRequestException("Falta header x-parameter-name"));
        }

        return ssm.getParameterValue(accessKeyId, secretAccessKey, sessionToken, region, parameterName)
                .map(mapper::toParameterValueResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
