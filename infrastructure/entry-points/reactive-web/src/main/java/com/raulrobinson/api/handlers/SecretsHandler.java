package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.SecretsMapper;
import com.raulrobinson.exception.SecretsBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.ISecretsUseCase;
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
public class SecretsHandler extends RequestValidation {

    private final ISecretsUseCase secrets;
    private final SecretsMapper mapper;

    // ── POST /api/secrets/list ────────────────────────────────────────────────
    public Mono<ServerResponse> listSecrets(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new SecretsBadRequestException("Faltan credenciales AWS"));
        }

        return secrets.listSecrets(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toSecretsResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/secrets/value ───────────────────────────────────────────────
    public Mono<ServerResponse> getSecretValue(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String secretId        = header(request, "x-secret-id");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new SecretsBadRequestException("Faltan credenciales AWS"));
        }
        if (secretId == null || secretId.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta header x-secret-id"));
        }

        return secrets.getSecretValue(accessKeyId, secretAccessKey, sessionToken, region, secretId)
                .map(mapper::toSecretValueResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
