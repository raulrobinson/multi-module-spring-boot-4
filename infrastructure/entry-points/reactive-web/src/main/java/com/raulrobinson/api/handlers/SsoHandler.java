package com.raulrobinson.api.handlers;

import com.raulrobinson.api.dto.SsoAccountsResponse;
import com.raulrobinson.api.dto.SsoRolesResponse;
import com.raulrobinson.api.mapper.SsoMapper;
import com.raulrobinson.exception.SsoBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.ISsoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsoHandler extends RequestValidation {

    private final ISsoUseCase sso;
    private final SsoMapper mapper;

    // ── POST /api/auth/sso/start ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> start(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    String startUrl = (String) body.get("startUrl");
                    String region   = regionOrDefault(body);

                    if (startUrl == null || startUrl.isBlank()) {
                        return Mono.error(new SsoBadRequestException("Falta el campo startUrl en el cuerpo"));
                    }

                    return sso.startDeviceAuth(startUrl, region)
                            .map(mapper::toDeviceAuthResponse)
                            .flatMap(dto -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dto));
                });
    }

    // ── POST /api/auth/sso/poll ──────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> poll(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    String clientId     = (String) body.get("clientId");
                    String clientSecret = (String) body.get("clientSecret");
                    String deviceCode   = (String) body.get("deviceCode");
                    String region       = regionOrDefault(body);

                    if (clientId == null || clientSecret == null || deviceCode == null) {
                        return Mono.error(new SsoBadRequestException(
                                "Faltan campos requeridos: clientId, clientSecret, deviceCode"));
                    }

                    return sso.pollToken(clientId, clientSecret, deviceCode, region)
                            .map(mapper::toTokenStatusResponse)
                            .flatMap(dto -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dto));
                });
    }

    // ── POST /api/auth/sso/accounts ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> accounts(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    String accessToken = (String) body.get("accessToken");
                    String region      = regionOrDefault(body);

                    if (accessToken == null || accessToken.isBlank()) {
                        return Mono.error(new SsoBadRequestException("Falta el campo accessToken en el cuerpo"));
                    }

                    return sso.listAccounts(accessToken, region)
                            .map(accounts -> SsoAccountsResponse.builder()
                                    .accounts(accounts.stream().map(mapper::toAccountResponse).toList())
                                    .build())
                            .flatMap(dto -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dto));
                });
    }

    // ── POST /api/auth/sso/roles ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> roles(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    String accessToken = (String) body.get("accessToken");
                    String accountId   = (String) body.get("accountId");
                    String region      = regionOrDefault(body);

                    if (accessToken == null || accountId == null) {
                        return Mono.error(new SsoBadRequestException(
                                "Faltan campos requeridos: accessToken, accountId"));
                    }

                    return sso.listRoles(accessToken, accountId, region)
                            .map(roles -> SsoRolesResponse.builder()
                                    .roles(roles.stream().map(mapper::toRoleResponse).toList())
                                    .build())
                            .flatMap(dto -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dto));
                });
    }

    // ── POST /api/auth/sso/credentials ──────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> credentials(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    String accessToken = (String) body.get("accessToken");
                    String accountId   = (String) body.get("accountId");
                    String roleName    = (String) body.get("roleName");
                    String region      = regionOrDefault(body);

                    if (accessToken == null || accountId == null || roleName == null) {
                        return Mono.error(new SsoBadRequestException(
                                "Faltan campos requeridos: accessToken, accountId, roleName"));
                    }

                    return sso.getCredentials(accessToken, accountId, roleName, region)
                            .map(mapper::toCredentialsResponse)
                            .flatMap(dto -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dto));
                });
    }

    private String regionOrDefault(Map<?, ?> body) {
        Object r = body.get("region");
        return r instanceof String s && !s.isBlank() ? s : "us-east-1";
    }
}
