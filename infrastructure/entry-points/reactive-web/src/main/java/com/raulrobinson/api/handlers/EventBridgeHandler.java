package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.EventBridgeMapper;
import com.raulrobinson.exception.EventBridgeBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.IEventBridgeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBridgeHandler extends RequestValidation {

    private final IEventBridgeUseCase eventBridge;
    private final EventBridgeMapper mapper;

    // ── POST /api/eventbridge/buses ───────────────────────────────────────────
    public Mono<ServerResponse> listBuses(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new EventBridgeBadRequestException("Faltan credenciales AWS"));
        }

        return eventBridge.listBuses(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toBusesResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/eventbridge/rules ───────────────────────────────────────────
    public Mono<ServerResponse> listRules(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String busName         = blankFallback(header(req, "x-eb-bus"), "default");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new EventBridgeBadRequestException("Faltan credenciales AWS"));
        }

        return eventBridge.listRules(accessKeyId, secretAccessKey, sessionToken, region, busName)
                .map(mapper::toRulesResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/eventbridge/rule-detail ─────────────────────────────────────
    public Mono<ServerResponse> getRuleDetail(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String busName         = blankFallback(header(req, "x-eb-bus"), "default");
        String ruleName        = header(req, "x-eb-rule");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new EventBridgeBadRequestException("Faltan credenciales AWS"));
        }
        if (ruleName == null || ruleName.isBlank()) {
            return Mono.error(new EventBridgeBadRequestException("Falta header x-eb-rule"));
        }

        return eventBridge.getRuleDetail(accessKeyId, secretAccessKey, sessionToken, region, busName, ruleName)
                .map(mapper::toRuleDetailResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/eventbridge/put-event ───────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> putEvent(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String busName         = blankFallback(header(req, "x-eb-bus"), "default");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new EventBridgeBadRequestException("Faltan credenciales AWS"));
        }

        return req.bodyToMono(Map.class)
                .flatMap(body -> {
                    String source     = (String) body.getOrDefault("source",     "");
                    String detailType = (String) body.getOrDefault("detailType", "");
                    String detail     = (String) body.getOrDefault("detail",     "{}");
                    Object resourcesRaw = body.get("resources");
                    List<String> resources = resourcesRaw instanceof List
                            ? (List<String>) resourcesRaw
                            : Collections.emptyList();

                    return eventBridge.putEvent(
                                    accessKeyId, secretAccessKey, sessionToken, region,
                                    busName, source, detailType, detail, resources)
                            .map(mapper::toPutResponse)
                            .flatMap(dto -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dto));
                });
    }
}
