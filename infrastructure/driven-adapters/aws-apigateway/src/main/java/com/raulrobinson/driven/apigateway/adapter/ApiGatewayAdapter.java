package com.raulrobinson.driven.apigateway.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.*;
import com.raulrobinson.ports.out.ApiGatewayGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2AsyncClient;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiGatewayAdapter implements ApiGatewayGateway {

    @Override
    public Mono<ApiGatewayApisResult> listApis(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region) {
        return Mono.defer(() -> {
            ApiGatewayAsyncClient v1 = buildV1Client(region, accessKeyId, secretAccessKey, sessionToken);
            ApiGatewayV2AsyncClient v2 = buildV2Client(region, accessKeyId, secretAccessKey, sessionToken);

            List<ApiGatewayApi> apis = Collections.synchronizedList(new ArrayList<>());
            List<String> errors = Collections.synchronizedList(new ArrayList<>());

            Mono<Void> restMono = listAllRestApis(v1, null, apis, region)
                    .onErrorResume(e -> {
                        errors.add("REST API (v1): " + rootCause(e));
                        return Mono.empty();
                    });

            Mono<Void> httpMono = listAllHttpApis(v2, null, apis)
                    .onErrorResume(e -> {
                        errors.add("HTTP/WS API (v2): " + rootCause(e));
                        return Mono.empty();
                    });

            return restMono.then(httpMono)
                    .then(Mono.fromCallable(() -> {
                        apis.sort(Comparator.comparing(a -> a.name().toLowerCase()));
                        return new ApiGatewayApisResult(new ArrayList<>(apis), new ArrayList<>(errors));
                    }))
                    .doFinally(signal -> {
                        v1.close();
                        v2.close();
                    });
        }).onErrorMap(this::mapException);
    }

    @Override
    public Mono<ApiGatewayStagesResult> listStages(String accessKeyId, String secretAccessKey,
                                                   String sessionToken, String region,
                                                   String apiId, String apiType) {
        return Mono.defer(() -> {
            List<ApiGatewayStage> stages = new ArrayList<>();

            if ("REST".equals(apiType)) {
                ApiGatewayAsyncClient v1 = buildV1Client(region, accessKeyId, secretAccessKey, sessionToken);
                return Mono.fromFuture(v1.getStages(GetStagesRequest.builder().restApiId(apiId).build()))
                        .map(resp -> {
                            for (var s : resp.item()) {
                                stages.add(new ApiGatewayStage(
                                        s.stageName(), s.description(), s.deploymentId(),
                                        s.lastUpdatedDate() != null ? s.lastUpdatedDate().toString() : null,
                                        s.createdDate() != null ? s.createdDate().toString() : null,
                                        "https://" + apiId + ".execute-api." + region + ".amazonaws.com/" + s.stageName(),
                                        null, null
                                ));
                            }
                            stages.sort(Comparator.comparing(ApiGatewayStage::name));
                            return new ApiGatewayStagesResult(stages);
                        })
                        .doFinally(signal -> v1.close());
            } else {
                ApiGatewayV2AsyncClient v2 = buildV2Client(region, accessKeyId, secretAccessKey, sessionToken);
                return listAllV2Stages(v2, apiId, null, stages, region)
                        .then(Mono.fromCallable(() -> {
                            stages.sort(Comparator.comparing(ApiGatewayStage::name));
                            return new ApiGatewayStagesResult(stages);
                        }))
                        .doFinally(signal -> v2.close());
            }
        }).onErrorMap(this::mapException);
    }

    @Override
    public Mono<ApiGatewayResourcesResult> listResources(String accessKeyId, String secretAccessKey,
                                                         String sessionToken, String region,
                                                         String apiId, String apiType) {
        return Mono.defer(() -> {
            List<ApiGatewayResource> items = new ArrayList<>();

            if ("REST".equals(apiType)) {
                ApiGatewayAsyncClient v1 = buildV1Client(region, accessKeyId, secretAccessKey, sessionToken);
                return listAllRestResources(v1, apiId, null, items)
                        .then(Mono.fromCallable(() -> {
                            items.sort(Comparator.comparing(r -> blankFallback(r.path(), "")));
                            return new ApiGatewayResourcesResult("REST", items);
                        }))
                        .doFinally(signal -> v1.close());
            } else {
                ApiGatewayV2AsyncClient v2 = buildV2Client(region, accessKeyId, secretAccessKey, sessionToken);
                return listAllV2Routes(v2, apiId, null, items)
                        .then(Mono.fromCallable(() -> {
                            items.sort(Comparator.comparing(r -> blankFallback(r.routeKey(), "")));
                            return new ApiGatewayResourcesResult(apiType, items);
                        }))
                        .doFinally(signal -> v2.close());
            }
        }).onErrorMap(this::mapException);
    }

    @Override
    public Mono<ApiGatewayKeysResult> listKeys(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region,
                                               String apiId, String apiType) {
        return Mono.defer(() -> {
            if (!"REST".equals(apiType)) {
                return Mono.just(new ApiGatewayKeysResult(
                        List.of(), "Las API Keys solo aplican a REST APIs (v1)", null));
            }

            ApiGatewayAsyncClient v1 = buildV1Client(region, accessKeyId, secretAccessKey, sessionToken);
            List<ApiGatewayKey> keys = new ArrayList<>();

            return collectLinkedKeyIds(v1, apiId)
                    .flatMap(linkedKeyIds -> {
                        if (!linkedKeyIds.isEmpty()) {
                            return fetchKeysByIds(v1, linkedKeyIds, keys);
                        } else {
                            return listAllApiKeys(v1, null, keys);
                        }
                    })
                    .then(Mono.fromCallable(() -> {
                        keys.sort(Comparator.comparing(k -> k.name().toLowerCase()));
                        return new ApiGatewayKeysResult(keys, null, null);
                    }))
                    .onErrorResume(e -> {
                        log.warn("Error listing API keys: {}", rootCause(e));
                        return Mono.just(new ApiGatewayKeysResult(keys, null, rootCause(e)));
                    })
                    .doFinally(signal -> v1.close());
        }).onErrorMap(this::mapException);
    }

    // ── Pagination helpers ────────────────────────────────────────────────────

    private Mono<Void> listAllRestApis(ApiGatewayAsyncClient v1, String position,
                                       List<ApiGatewayApi> acc, String region) {
        var builder = GetRestApisRequest.builder().limit(500);
        if (position != null) builder.position(position);

        return Mono.fromFuture(v1.getRestApis(builder.build()))
                .flatMap(resp -> {
                    for (var api : resp.items()) {
                        acc.add(new ApiGatewayApi(
                                api.id(), api.name(), "REST", api.description(),
                                api.createdDate() != null ? api.createdDate().toString() : null,
                                "https://" + api.id() + ".execute-api." + region + ".amazonaws.com",
                                api.endpointConfiguration() != null
                                        ? api.endpointConfiguration().typesAsStrings().stream().findFirst().orElse(null)
                                        : null
                        ));
                    }
                    if (resp.position() != null) {
                        return listAllRestApis(v1, resp.position(), acc, region);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> listAllHttpApis(ApiGatewayV2AsyncClient v2, String nextToken,
                                       List<ApiGatewayApi> acc) {
        var builder = software.amazon.awssdk.services.apigatewayv2.model.GetApisRequest.builder();
        if (nextToken != null) builder.nextToken(nextToken);

        return Mono.fromFuture(v2.getApis(builder.build()))
                .flatMap(resp -> {
                    for (var api : resp.items()) {
                        acc.add(new ApiGatewayApi(
                                api.apiId(), api.name(), api.protocolTypeAsString(),
                                api.description(),
                                api.createdDate() != null ? api.createdDate().toString() : null,
                                api.apiEndpoint(), "REGIONAL"
                        ));
                    }
                    if (resp.nextToken() != null) {
                        return listAllHttpApis(v2, resp.nextToken(), acc);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> listAllV2Stages(ApiGatewayV2AsyncClient v2, String apiId,
                                       String nextToken, List<ApiGatewayStage> acc, String region) {
        var builder = software.amazon.awssdk.services.apigatewayv2.model.GetStagesRequest.builder().apiId(apiId);
        if (nextToken != null) builder.nextToken(nextToken);

        return Mono.fromFuture(v2.getStages(builder.build()))
                .flatMap(resp -> {
                    for (var s : resp.items()) {
                        Double rateLimit = null;
                        Double burstLimit = null;
                        if (s.defaultRouteSettings() != null) {
                            rateLimit = s.defaultRouteSettings().throttlingRateLimit();
                            burstLimit = s.defaultRouteSettings().throttlingBurstLimit() != null
                                    ? s.defaultRouteSettings().throttlingBurstLimit().doubleValue()
                                    : null;
                        }
                        acc.add(new ApiGatewayStage(
                                s.stageName(), s.description(), s.deploymentId(),
                                s.lastUpdatedDate() != null ? s.lastUpdatedDate().toString() : null,
                                s.createdDate() != null ? s.createdDate().toString() : null,
                                "https://" + apiId + ".execute-api." + region + ".amazonaws.com/" + s.stageName(),
                                rateLimit, burstLimit
                        ));
                    }
                    if (resp.nextToken() != null) {
                        return listAllV2Stages(v2, apiId, resp.nextToken(), acc, region);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> listAllRestResources(ApiGatewayAsyncClient v1, String apiId,
                                            String position, List<ApiGatewayResource> acc) {
        var builder = GetResourcesRequest.builder().restApiId(apiId).limit(500).embed(List.of("methods"));
        if (position != null) builder.position(position);

        return Mono.fromFuture(v1.getResources(builder.build()))
                .flatMap(resp -> {
                    for (var r : resp.items()) {
                        List<String> methods = new ArrayList<>();
                        if (r.resourceMethods() != null) {
                            methods.addAll(r.resourceMethods().keySet().stream().sorted().collect(Collectors.toList()));
                        }
                        acc.add(new ApiGatewayResource(
                                r.id(), r.path(), r.pathPart(), r.parentId(),
                                methods, null, null, null
                        ));
                    }
                    if (resp.position() != null) {
                        return listAllRestResources(v1, apiId, resp.position(), acc);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> listAllV2Routes(ApiGatewayV2AsyncClient v2, String apiId,
                                       String nextToken, List<ApiGatewayResource> acc) {
        var builder = software.amazon.awssdk.services.apigatewayv2.model.GetRoutesRequest.builder().apiId(apiId);
        if (nextToken != null) builder.nextToken(nextToken);

        return Mono.fromFuture(v2.getRoutes(builder.build()))
                .flatMap(resp -> {
                    for (var route : resp.items()) {
                        String rk = route.routeKey();
                        List<String> methods = new ArrayList<>();
                        String path = rk;
                        if (rk != null && rk.contains(" ")) {
                            methods.add(rk.split(" ")[0]);
                            path = rk.substring(rk.indexOf(' ') + 1);
                        }
                        acc.add(new ApiGatewayResource(
                                route.routeId(), path, null, null,
                                methods, rk, route.target(), route.authorizationTypeAsString()
                        ));
                    }
                    if (resp.nextToken() != null) {
                        return listAllV2Routes(v2, apiId, resp.nextToken(), acc);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Set<String>> collectLinkedKeyIds(ApiGatewayAsyncClient v1, String apiId) {
        Set<String> keyIds = new LinkedHashSet<>();
        return listAllUsagePlans(v1, null, keyIds, apiId)
                .thenReturn(keyIds)
                .onErrorReturn(new LinkedHashSet<>());
    }

    private Mono<Void> listAllUsagePlans(ApiGatewayAsyncClient v1, String position,
                                         Set<String> keyIds, String apiId) {
        var builder = GetUsagePlansRequest.builder().limit(500);
        if (position != null) builder.position(position);

        return Mono.fromFuture(v1.getUsagePlans(builder.build()))
                .flatMap(resp -> {
                    List<Mono<Void>> planMonos = new ArrayList<>();
                    for (var plan : resp.items()) {
                        boolean hasApi = apiId == null || plan.apiStages().stream()
                                .anyMatch(s -> apiId.equals(s.apiId()));
                        if (hasApi) {
                            planMonos.add(listAllPlanKeys(v1, plan.id(), null, keyIds));
                        }
                    }
                    Mono<Void> plansMono = planMonos.isEmpty()
                            ? Mono.empty()
                            : Mono.when(planMonos);
                    if (resp.position() != null) {
                        return plansMono.then(listAllUsagePlans(v1, resp.position(), keyIds, apiId));
                    }
                    return plansMono;
                });
    }

    private Mono<Void> listAllPlanKeys(ApiGatewayAsyncClient v1, String planId,
                                       String position, Set<String> keyIds) {
        var builder = GetUsagePlanKeysRequest.builder().usagePlanId(planId).limit(500);
        if (position != null) builder.position(position);

        return Mono.fromFuture(v1.getUsagePlanKeys(builder.build()))
                .flatMap(resp -> {
                    resp.items().forEach(k -> keyIds.add(k.id()));
                    if (resp.position() != null) {
                        return listAllPlanKeys(v1, planId, resp.position(), keyIds);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> fetchKeysByIds(ApiGatewayAsyncClient v1, Set<String> keyIds,
                                      List<ApiGatewayKey> keys) {
        List<Mono<Void>> fetches = keyIds.stream().map(keyId ->
                Mono.fromFuture(v1.getApiKey(GetApiKeyRequest.builder()
                                .apiKey(keyId).includeValue(true).build()))
                        .doOnNext(k -> keys.add(new ApiGatewayKey(
                                k.id(),
                                k.name() != null ? k.name() : "",
                                k.value() != null ? k.value() : "",
                                k.enabled() != null && k.enabled(),
                                k.description()
                        )))
                        .<Void>then()
                        .onErrorResume(e -> {
                            log.warn("Could not fetch key {}: {}", keyId, e.getMessage());
                            return Mono.empty();
                        })
        ).toList();
        return fetches.isEmpty() ? Mono.empty() : Mono.when(fetches);
    }

    private Mono<Void> listAllApiKeys(ApiGatewayAsyncClient v1, String position,
                                      List<ApiGatewayKey> keys) {
        var builder = GetApiKeysRequest.builder().includeValues(true).limit(500);
        if (position != null) builder.position(position);

        return Mono.fromFuture(v1.getApiKeys(builder.build()))
                .flatMap(resp -> {
                    for (var k : resp.items()) {
                        keys.add(new ApiGatewayKey(
                                k.id(),
                                k.name() != null ? k.name() : "",
                                k.value() != null ? k.value() : "",
                                k.enabled() != null && k.enabled(),
                                k.description()
                        ));
                    }
                    if (resp.position() != null) {
                        return listAllApiKeys(v1, resp.position(), keys);
                    }
                    return Mono.empty();
                });
    }

    // ── Client builders ───────────────────────────────────────────────────────

    private ApiGatewayAsyncClient buildV1Client(String region, String accessKeyId,
                                                String secretAccessKey, String sessionToken) {
        return ApiGatewayAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(buildCreds(accessKeyId, secretAccessKey, sessionToken)))
                .build();
    }

    private ApiGatewayV2AsyncClient buildV2Client(String region, String accessKeyId,
                                                  String secretAccessKey, String sessionToken) {
        return ApiGatewayV2AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(buildCreds(accessKeyId, secretAccessKey, sessionToken)))
                .build();
    }

    private AwsCredentials buildCreds(String accessKeyId, String secretAccessKey, String sessionToken) {
        return (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }

    private String blankFallback(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private String rootCause(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof ApiGatewayBadRequestException
                || cause instanceof ApiGatewayClientException
                || cause instanceof ApiGatewayAccessDeniedException) {
            return cause;
        }

        if (cause instanceof software.amazon.awssdk.services.apigateway.model.UnauthorizedException
                || cause instanceof software.amazon.awssdk.services.apigateway.model.BadRequestException) {
            return new ApiGatewayAccessDeniedException("Credenciales AWS no válidas o acceso denegado", cause);
        }

        if (cause instanceof SdkClientException) {
            return new ApiGatewayClientException("Error de comunicación con AWS API Gateway", cause);
        }

        return new ApiGatewayClientException("Error inesperado consultando AWS API Gateway", cause);
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
