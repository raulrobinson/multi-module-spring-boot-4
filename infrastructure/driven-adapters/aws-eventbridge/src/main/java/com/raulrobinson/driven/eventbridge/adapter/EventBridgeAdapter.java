package com.raulrobinson.driven.eventbridge.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.*;
import com.raulrobinson.ports.out.EventBridgeGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventBridgeAdapter implements EventBridgeGateway {

    @Override
    public Mono<EventBusesResult> listBuses(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    EventBridgeAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return listAllBuses(client, null, new ArrayList<>())
                            .map(buses -> {
                                buses.sort(Comparator.comparing(b -> b.name().toLowerCase()));
                                return new EventBusesResult(buses);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<EventRulesResult> listRules(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region, String busName) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    EventBridgeAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    String bus = blankFallback(busName, "default");
                    return listAllRules(client, bus, null, new ArrayList<>())
                            .map(rules -> {
                                rules.sort(Comparator.comparing(r -> r.name().toLowerCase()));
                                return new EventRulesResult(rules);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<EventRuleDetail> getRuleDetail(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region,
                                               String busName, String ruleName) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    EventBridgeAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    String bus = blankFallback(busName, "default");

                    Mono<DescribeRuleResponse> describeMono = Mono.fromFuture(
                            client.describeRule(DescribeRuleRequest.builder()
                                    .name(ruleName).eventBusName(bus).build()));

                    Mono<ListTargetsByRuleResponse> targetsMono = Mono.fromFuture(
                            client.listTargetsByRule(ListTargetsByRuleRequest.builder()
                                    .rule(ruleName).eventBusName(bus).build()));

                    return describeMono.zipWith(targetsMono, (dr, tr) -> {
                                List<EventRuleTarget> targets = tr.targets().stream().map(t ->
                                        new EventRuleTarget(
                                                t.id(), t.arn(), t.input(), t.inputPath(), t.roleArn(),
                                                t.inputTransformer() != null ? t.inputTransformer().inputTemplate() : null
                                        )).collect(Collectors.toList());

                                return new EventRuleDetail(
                                        dr.name(), dr.arn(), dr.stateAsString(), dr.description(),
                                        dr.scheduleExpression(), dr.eventPattern(),
                                        dr.eventBusName(), dr.managedBy(), dr.roleArn(), targets
                                );
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<EventPutResult> putEvent(String accessKeyId, String secretAccessKey,
                                         String sessionToken, String region,
                                         String busName, String source,
                                         String detailType, String detail,
                                         List<String> resources) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    EventBridgeAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    String bus = blankFallback(busName, "default");
                    List<String> res = resources != null ? resources : Collections.emptyList();

                    var entryBuilder = PutEventsRequestEntry.builder()
                            .eventBusName(bus)
                            .source(source)
                            .detailType(detailType)
                            .detail(detail);
                    if (!res.isEmpty()) entryBuilder.resources(res);

                    return Mono.fromFuture(client.putEvents(
                                    PutEventsRequest.builder().entries(entryBuilder.build()).build()))
                            .map(resp -> {
                                List<EventPutEntry> entries = resp.entries().stream().map(e ->
                                        new EventPutEntry(
                                                e.eventId(), e.errorCode(), e.errorMessage(),
                                                e.errorCode() == null
                                        )).collect(Collectors.toList());
                                return new EventPutResult(resp.failedEntryCount(), entries);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    // ── Pagination helpers ─────────────────────────────────────────────────────

    private Mono<List<EventBusItem>> listAllBuses(EventBridgeAsyncClient client,
                                                  String nextToken, List<EventBusItem> acc) {
        var builder = ListEventBusesRequest.builder().limit(100);
        if (nextToken != null) builder.nextToken(nextToken);

        return Mono.fromFuture(client.listEventBuses(builder.build()))
                .flatMap(resp -> {
                    resp.eventBuses().forEach(bus ->
                            acc.add(new EventBusItem(bus.name(), bus.arn(), bus.policy() != null)));
                    if (resp.nextToken() != null) {
                        return listAllBuses(client, resp.nextToken(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    private Mono<List<EventRuleItem>> listAllRules(EventBridgeAsyncClient client,
                                                   String bus, String nextToken,
                                                   List<EventRuleItem> acc) {
        var builder = ListRulesRequest.builder().eventBusName(bus).limit(100);
        if (nextToken != null) builder.nextToken(nextToken);

        return Mono.fromFuture(client.listRules(builder.build()))
                .flatMap(resp -> {
                    resp.rules().forEach(rule ->
                            acc.add(new EventRuleItem(
                                    rule.name(), rule.arn(), rule.stateAsString(),
                                    rule.description(), rule.scheduleExpression(),
                                    rule.eventPattern() != null && !rule.eventPattern().isBlank(),
                                    rule.managedBy()
                            )));
                    if (resp.nextToken() != null) {
                        return listAllRules(client, bus, resp.nextToken(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    // ── Client builder ────────────────────────────────────────────────────────

    private EventBridgeAsyncClient buildClient(String region, String accessKeyId,
                                               String secretAccessKey, String sessionToken) {
        var credentials = (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return EventBridgeAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private String blankFallback(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private Mono<Void> validate(String accessKeyId, String secretAccessKey, String region) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new EventBridgeBadRequestException("Falta accessKeyId"));
        }
        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new EventBridgeBadRequestException("Falta secretAccessKey"));
        }
        if (region == null || region.isBlank()) {
            return Mono.error(new EventBridgeBadRequestException("Falta region AWS"));
        }
        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof EventBridgeBadRequestException
                || cause instanceof EventBridgeClientException
                || cause instanceof EventBridgeAccessDeniedException) {
            return cause;
        }

        if (cause instanceof software.amazon.awssdk.services.eventbridge.model.ResourceNotFoundException) {
            return new EventBridgeClientException("Recurso EventBridge no encontrado", cause);
        }

        if (cause instanceof SdkClientException) {
            return new EventBridgeClientException("Error de comunicación con AWS EventBridge", cause);
        }

        return new EventBridgeClientException("Error inesperado consultando AWS EventBridge", cause.getMessage());
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
