package com.raulrobinson.driven.secrets.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.Secret;
import com.raulrobinson.model.SecretValue;
import com.raulrobinson.ports.out.SecretsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.*;
import software.amazon.awssdk.services.ssooidc.model.AccessDeniedException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class SecretsAdapter implements SecretsGateway {

    @Override
    public Mono<List<Secret>> listSecrets(String accessKeyId,
                                          String secretAccessKey,
                                          String sessionToken,
                                          String region) {

        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    SecretsManagerAsyncClient client = buildClient(
                            region,
                            accessKeyId,
                            secretAccessKey,
                            sessionToken
                    );

                    return listAllSecrets(client, null, new ArrayList<>())
                            .map(secrets -> {
                                secrets.sort(Comparator.comparing(s -> s.name().toLowerCase()));
                                return secrets;
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<SecretValue> getSecretValue(String accessKeyId,
                                            String secretAccessKey,
                                            String sessionToken,
                                            String region,
                                            String secretId) {

        return validate(accessKeyId, secretAccessKey, region)
                .then(validateSecretId(secretId))
                .then(Mono.defer(() -> {
                    SecretsManagerAsyncClient client = buildClient(
                            region,
                            accessKeyId,
                            secretAccessKey,
                            sessionToken
                    );

                    GetSecretValueRequest request = GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .build();

                    return Mono.fromFuture(client.getSecretValue(request))
                            .map(response -> new SecretValue(
                                    response.name(),
                                    response.arn(),
                                    response.secretString(),
                                    response.versionId(),
                                    response.versionStages(),
                                    response.createdDate() != null
                                            ? response.createdDate().toString()
                                            : null
                            ))
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    private Mono<Void> validateSecretId(String secretId) {
        if (secretId == null || secretId.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta secretId"));
        }

        return Mono.empty();
    }

    private Mono<List<Secret>> listAllSecrets(SecretsManagerAsyncClient client,
                                              String nextToken,
                                              List<Secret> accumulator) {

        ListSecretsRequest.Builder request = ListSecretsRequest.builder()
                .maxResults(100);

        if (nextToken != null && !nextToken.isBlank()) {
            request.nextToken(nextToken);
        }

        return Mono.fromFuture(client.listSecrets(request.build()))
                .flatMap(response -> {
                    response.secretList()
                            .stream()
                            .map(this::toSecret)
                            .forEach(accumulator::add);

                    if (response.nextToken() != null && !response.nextToken().isBlank()) {
                        return listAllSecrets(client, response.nextToken(), accumulator);
                    }

                    return Mono.just(accumulator);
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Secret toSecret(SecretListEntry entry) {
        return new Secret(
                entry.name(),
                entry.arn(),
                entry.description(),
                entry.lastChangedDate() != null ? entry.lastChangedDate().toString() : null,
                entry.lastAccessedDate() != null ? entry.lastAccessedDate().toString() : null,
                entry.createdDate() != null ? entry.createdDate().toString() : null,
                entry.deletedDate() != null ? entry.deletedDate().toString() : null
        );
    }

    private SecretsManagerAsyncClient buildClient(String region,
                                                  String accessKeyId,
                                                  String secretAccessKey,
                                                  String sessionToken) {

        var credentials = sessionToken != null && !sessionToken.isBlank()
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return SecretsManagerAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private Mono<Void> validate(String accessKeyId,
                                String secretAccessKey,
                                String region) {

        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta accessKeyId"));
        }

        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta secretAccessKey"));
        }

        if (region == null || region.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta region AWS"));
        }

        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof SecretsBadRequestException
                || cause instanceof SecretsAccessDeniedException
                || cause instanceof SecretsClientException
                || cause instanceof SecretsTimeoutException) {
            return cause;
        }

        if (cause instanceof AccessDeniedException
                || cause instanceof UnrecognizedClientException
                || cause instanceof InvalidRequestException
                || cause instanceof InvalidParameterException) {
            return new SecretsAccessDeniedException("No tiene permisos o las credenciales AWS no son válidas", cause);
        }

        if (cause instanceof ResourceNotFoundException) {
            return new SecretsClientException("No se encontró el recurso solicitado en Secrets Manager", cause);
        }

        if (cause instanceof SdkClientException) {
            return new SecretsClientException("Error de comunicación con AWS Secrets Manager", cause);
        }

        return new SecretsClientException("Error inesperado consultando AWS Secrets Manager", cause);
    }

    private Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }

        if (error.getCause() instanceof CompletionException completionException
                && completionException.getCause() != null) {
            return completionException.getCause();
        }

        return error;
    }
}