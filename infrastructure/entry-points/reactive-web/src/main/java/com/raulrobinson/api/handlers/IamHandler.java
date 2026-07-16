package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.IamMapper;
import com.raulrobinson.exception.IamBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.IIamUseCase;
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
public class IamHandler extends RequestValidation {

    private final IIamUseCase iam;
    private final IamMapper mapper;

    // ── POST /business/v1/api/iam/users ────────────────────────────────────────────────────
    public Mono<ServerResponse> listUsers(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new IamBadRequestException("Faltan credenciales AWS"));
        }

        return iam.listUsers(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toUsersResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /business/v1/api/iam/roles ────────────────────────────────────────────────────
    public Mono<ServerResponse> listRoles(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new IamBadRequestException("Faltan credenciales AWS"));
        }

        return iam.listRoles(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toRolesResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
