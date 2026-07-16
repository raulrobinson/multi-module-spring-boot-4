package com.raulrobinson.driven.ssm.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.SsmParameter;
import com.raulrobinson.model.SsmParametersResult;
import com.raulrobinson.model.SsmParameterValue;
import com.raulrobinson.ports.out.SsmGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class SsmAdapter implements SsmGateway {

    @Override
    public Mono<SsmParametersResult> listParameters(String accessKeyId, String secretAccessKey,
                                                    String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    SsmAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return listAllParameters(client, null, new ArrayList<>())
                            .map(params -> {
                                params.sort(Comparator.comparing(p -> p.name().toLowerCase()));
                                return new SsmParametersResult(params);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<SsmParameterValue> getParameterValue(String accessKeyId, String secretAccessKey,
                                                     String sessionToken, String region,
                                                     String parameterName) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(validateParameterName(parameterName))
                .then(Mono.defer(() -> {
                    SsmAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return Mono.fromFuture(client.getParameter(GetParameterRequest.builder()
                                    .name(parameterName)
                                    .withDecryption(true)
                                    .build()))
                            .map(resp -> {
                                var param = resp.parameter();
                                return new SsmParameterValue(
                                        param.name(),
                                        param.typeAsString(),
                                        param.value(),
                                        param.version(),
                                        param.lastModifiedDate() != null ? param.lastModifiedDate().toString() : null,
                                        param.arn(),
                                        param.dataType()
                                );
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    // ── Pagination helper ─────────────────────────────────────────────────────

    private Mono<List<SsmParameter>> listAllParameters(SsmAsyncClient client,
                                                       String nextToken,
                                                       List<SsmParameter> acc) {
        DescribeParametersRequest.Builder builder = DescribeParametersRequest.builder().maxResults(50);
        if (nextToken != null) builder.nextToken(nextToken);

        return Mono.fromFuture(client.describeParameters(builder.build()))
                .flatMap(resp -> {
                    for (ParameterMetadata meta : resp.parameters()) {
                        acc.add(new SsmParameter(
                                meta.name(),
                                meta.typeAsString(),
                                meta.description() != null ? meta.description() : "",
                                meta.lastModifiedDate() != null ? meta.lastModifiedDate().toString() : null,
                                meta.version(),
                                meta.tierAsString(),
                                meta.dataType()
                        ));
                    }
                    if (resp.nextToken() != null) {
                        return listAllParameters(client, resp.nextToken(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SsmAsyncClient buildClient(String region, String accessKeyId,
                                       String secretAccessKey, String sessionToken) {
        var credentials = (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return SsmAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private Mono<Void> validate(String accessKeyId, String secretAccessKey, String region) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new SsmBadRequestException("Falta accessKeyId"));
        }
        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new SsmBadRequestException("Falta secretAccessKey"));
        }
        if (region == null || region.isBlank()) {
            return Mono.error(new SsmBadRequestException("Falta region AWS"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateParameterName(String parameterName) {
        if (parameterName == null || parameterName.isBlank()) {
            return Mono.error(new SsmBadRequestException("Falta x-parameter-name"));
        }
        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof SsmBadRequestException
                || cause instanceof SsmClientException
                || cause instanceof SsmAccessDeniedException) {
            return cause;
        }

        if (cause instanceof ParameterNotFoundException) {
            return new SsmClientException("Parámetro SSM no encontrado", cause);
        }

        if (cause instanceof software.amazon.awssdk.services.ssm.model.InvalidKeyIdException
                || cause instanceof software.amazon.awssdk.services.ssm.model.InternalServerErrorException) {
            return new SsmAccessDeniedException("Acceso denegado o credenciales inválidas en SSM", cause);
        }

        if (cause instanceof SdkClientException) {
            return new SsmClientException("Error de comunicación con AWS SSM", cause);
        }

        return new SsmClientException("Error inesperado consultando AWS SSM", cause);
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
