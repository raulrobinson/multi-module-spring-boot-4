package com.raulrobinson.driven.lambda.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.LambdaFunction;
import com.raulrobinson.model.LambdaFunctionsResult;
import com.raulrobinson.model.LambdaInvokeResult;
import com.raulrobinson.ports.out.LambdaGateway;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class LambdaAdapter implements LambdaGateway {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Mono<LambdaInvokeResult> invokeFunction(String accessKeyId, String secretAccessKey,
                                                   String sessionToken, String region,
                                                   String functionArn, String payload) {
        return validate(accessKeyId, secretAccessKey)
                .then(validateArn(functionArn))
                .then(Mono.defer(() -> {
                    LambdaAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    String body = (payload != null && !payload.isBlank()) ? payload : "{}";

                    return Mono.fromFuture(client.invoke(InvokeRequest.builder()
                                    .functionName(functionArn)
                                    .payload(SdkBytes.fromString(body, StandardCharsets.UTF_8))
                                    .build()))
                            .map(resp -> {
                                String rawPayload = resp.payload() != null
                                        ? resp.payload().asUtf8String()
                                        : "null";
                                Object parsed;
                                try {
                                    parsed = MAPPER.readValue(rawPayload, Object.class);
                                } catch (Exception ex) {
                                    parsed = rawPayload;
                                }
                                return new LambdaInvokeResult(parsed);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<LambdaFunctionsResult> listFunctions(String accessKeyId, String secretAccessKey,
                                                     String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey)
                .then(Mono.defer(() -> {
                    LambdaAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return listAllFunctions(client, null, new ArrayList<>())
                            .map(functions -> {
                                functions.sort(Comparator.comparing(f -> f.fnName().toLowerCase()));
                                return new LambdaFunctionsResult(functions);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    // ── Pagination helper ─────────────────────────────────────────────────────

    private Mono<List<LambdaFunction>> listAllFunctions(LambdaAsyncClient client,
                                                        String marker,
                                                        List<LambdaFunction> acc) {
        ListFunctionsRequest.Builder builder = ListFunctionsRequest.builder().maxItems(50);
        if (marker != null) builder.marker(marker);

        return Mono.fromFuture(client.listFunctions(builder.build()))
                .flatMap(resp -> {
                    resp.functions().forEach(fn ->
                            acc.add(new LambdaFunction(fn.functionName(), fn.functionArn())));
                    if (resp.nextMarker() != null) {
                        return listAllFunctions(client, resp.nextMarker(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    // ── Client builder ────────────────────────────────────────────────────────

    private LambdaAsyncClient buildClient(String region, String accessKeyId,
                                          String secretAccessKey, String sessionToken) {
        var credentials = (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return LambdaAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private Mono<Void> validate(String accessKeyId, String secretAccessKey) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new LambdaBadRequestException("Falta accessKeyId"));
        }
        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new LambdaBadRequestException("Falta secretAccessKey"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateArn(String functionArn) {
        if (functionArn == null || functionArn.isBlank()) {
            return Mono.error(new LambdaBadRequestException("Falta x-lambda-arn"));
        }
        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof LambdaBadRequestException
                || cause instanceof LambdaClientException
                || cause instanceof LambdaAccessDeniedException) {
            return cause;
        }

        if (cause instanceof ResourceNotFoundException) {
            return new LambdaClientException("Función Lambda no encontrada", cause);
        }

        if (cause instanceof software.amazon.awssdk.services.lambda.model.InvalidRequestContentException) {
            return new LambdaBadRequestException("Payload inválido para la función Lambda", cause);
        }

        if (cause instanceof SdkClientException) {
            return new LambdaClientException("Error de comunicación con AWS Lambda", cause);
        }

        return new LambdaClientException("Error inesperado consultando AWS Lambda", cause);
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
