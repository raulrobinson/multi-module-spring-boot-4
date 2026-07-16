package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.ApiGatewayMapper;
import com.raulrobinson.exception.ApiGatewayBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.IApiGatewayUseCase;
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
public class ApiGatewayHandler extends RequestValidation {

    private final IApiGatewayUseCase apiGateway;
    private final ApiGatewayMapper mapper;

    // ── POST /api/apigw/apis ─────────────────────────────────────────────────
    public Mono<ServerResponse> listApis(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new ApiGatewayBadRequestException("Faltan credenciales AWS"));
        }

        return apiGateway.listApis(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toApisResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/apigw/stages ───────────────────────────────────────────────
    public Mono<ServerResponse> listStages(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String apiId           = header(req, "x-apigw-id");
        String apiType         = blankFallback(header(req, "x-apigw-type"), "REST");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new ApiGatewayBadRequestException("Faltan credenciales AWS"));
        }
        if (apiId == null || apiId.isBlank()) {
            return Mono.error(new ApiGatewayBadRequestException("Falta header x-apigw-id"));
        }

        return apiGateway.listStages(accessKeyId, secretAccessKey, sessionToken, region, apiId, apiType)
                .map(mapper::toStagesResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/apigw/resources ────────────────────────────────────────────
    public Mono<ServerResponse> listResources(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String apiId           = header(req, "x-apigw-id");
        String apiType         = blankFallback(header(req, "x-apigw-type"), "REST");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new ApiGatewayBadRequestException("Faltan credenciales AWS"));
        }
        if (apiId == null || apiId.isBlank()) {
            return Mono.error(new ApiGatewayBadRequestException("Falta header x-apigw-id"));
        }

        return apiGateway.listResources(accessKeyId, secretAccessKey, sessionToken, region, apiId, apiType)
                .map(mapper::toResourcesResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/apigw/keys ─────────────────────────────────────────────────
    public Mono<ServerResponse> listKeys(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String apiId           = header(req, "x-apigw-id");
        String apiType         = blankFallback(header(req, "x-apigw-type"), "REST");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new ApiGatewayBadRequestException("Faltan credenciales AWS"));
        }

        return apiGateway.listKeys(accessKeyId, secretAccessKey, sessionToken, region, apiId, apiType)
                .map(mapper::toKeysResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
