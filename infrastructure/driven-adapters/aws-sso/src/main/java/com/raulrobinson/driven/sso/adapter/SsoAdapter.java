package com.raulrobinson.driven.sso.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.*;
import com.raulrobinson.ports.out.SsoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sso.SsoAsyncClient;
import software.amazon.awssdk.services.ssooidc.SsoOidcAsyncClient;
import software.amazon.awssdk.services.ssooidc.model.*;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class SsoAdapter implements SsoGateway {

    @Override
    public Mono<SsoDeviceAuth> startDeviceAuth(String startUrl, String region) {
        return Mono.defer(() -> {
            SsoOidcAsyncClient client = buildOidcClient(region);

            return Mono.fromFuture(client.registerClient(r -> r
                            .clientName("aws-config-dashboard")
                            .clientType("public")))
                    .flatMap(reg -> Mono.fromFuture(client.startDeviceAuthorization(r -> r
                                    .clientId(reg.clientId())
                                    .clientSecret(reg.clientSecret())
                                    .startUrl(startUrl)))
                            .map(auth -> new SsoDeviceAuth(
                                    reg.clientId(),
                                    reg.clientSecret(),
                                    auth.deviceCode(),
                                    auth.userCode(),
                                    auth.verificationUri(),
                                    auth.verificationUriComplete(),
                                    auth.expiresIn(),
                                    auth.interval()
                            )))
                    .doFinally(signal -> client.close());
        }).onErrorMap(this::mapException);
    }

    @Override
    public Mono<SsoTokenResult> pollToken(String clientId, String clientSecret,
                                          String deviceCode, String region) {
        return Mono.defer(() -> {
            SsoOidcAsyncClient client = buildOidcClient(region);

            return Mono.fromFuture(client.createToken(r -> r
                            .clientId(clientId)
                            .clientSecret(clientSecret)
                            .grantType("urn:ietf:params:oauth:grant-type:device_code")
                            .deviceCode(deviceCode)))
                    .map(token -> new SsoTokenResult("success", token.accessToken()))
                    .onErrorResume(error -> {
                        Throwable cause = unwrap(error);
                        if (cause instanceof AuthorizationPendingException)
                            return Mono.just(new SsoTokenResult("pending", null));
                        if (cause instanceof SlowDownException)
                            return Mono.just(new SsoTokenResult("slow_down", null));
                        if (cause instanceof ExpiredTokenException)
                            return Mono.just(new SsoTokenResult("expired", null));
                        if (cause instanceof AccessDeniedException)
                            return Mono.just(new SsoTokenResult("denied", null));
                        return Mono.error(mapException(error));
                    })
                    .doFinally(signal -> client.close());
        });
    }

    @Override
    public Mono<List<SsoAccount>> listAccounts(String accessToken, String region) {
        return Mono.defer(() -> {
            SsoAsyncClient client = buildSsoClient(region);

            return Mono.fromFuture(client.listAccounts(r -> r
                            .accessToken(accessToken)
                            .maxResults(100)))
                    .map(resp -> resp.accountList().stream()
                            .map(acc -> new SsoAccount(acc.accountId(), acc.accountName(), acc.emailAddress()))
                            .toList())
                    .doFinally(signal -> client.close());
        }).onErrorMap(this::mapException);
    }

    @Override
    public Mono<List<SsoRole>> listRoles(String accessToken, String accountId, String region) {
        return Mono.defer(() -> {
            SsoAsyncClient client = buildSsoClient(region);

            return Mono.fromFuture(client.listAccountRoles(r -> r
                            .accessToken(accessToken)
                            .accountId(accountId)))
                    .map(resp -> resp.roleList().stream()
                            .map(role -> new SsoRole(role.roleName(), role.accountId()))
                            .toList())
                    .doFinally(signal -> client.close());
        }).onErrorMap(this::mapException);
    }

    @Override
    public Mono<SsoCredentials> getCredentials(String accessToken, String accountId,
                                               String roleName, String region) {
        return Mono.defer(() -> {
            SsoAsyncClient client = buildSsoClient(region);

            return Mono.fromFuture(client.getRoleCredentials(r -> r
                            .accessToken(accessToken)
                            .accountId(accountId)
                            .roleName(roleName)))
                    .map(resp -> {
                        var creds = resp.roleCredentials();
                        String expiration = creds.expiration() != null
                                ? Instant.ofEpochMilli(creds.expiration()).toString()
                                : null;
                        return new SsoCredentials(
                                creds.accessKeyId(),
                                creds.secretAccessKey(),
                                creds.sessionToken(),
                                expiration
                        );
                    })
                    .doFinally(signal -> client.close());
        }).onErrorMap(this::mapException);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SsoOidcAsyncClient buildOidcClient(String region) {
        return SsoOidcAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
    }

    private SsoAsyncClient buildSsoClient(String region) {
        return SsoAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof SsoBadRequestException
                || cause instanceof SsoClientException
                || cause instanceof SsoAccessDeniedException) {
            return cause;
        }

        if (cause instanceof AccessDeniedException) {
            return new SsoAccessDeniedException("Acceso denegado en SSO", cause);
        }

        if (cause instanceof SdkClientException) {
            return new SsoClientException("Error de comunicación con AWS SSO", cause);
        }

        return new SsoClientException("Error inesperado en AWS SSO", cause);
    }

    private Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }
        if (error.getCause() instanceof CompletionException ce && ce.getCause() != null) {
            return ce.getCause();
        }
        return error;
    }
}
