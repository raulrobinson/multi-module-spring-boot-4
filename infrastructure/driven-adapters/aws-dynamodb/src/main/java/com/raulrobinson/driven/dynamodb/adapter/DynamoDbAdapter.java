package com.raulrobinson.driven.dynamodb.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.*;
import com.raulrobinson.ports.out.DynamoDbGateway;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.ssooidc.model.AccessDeniedException;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DynamoDbAdapter implements DynamoDbGateway {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    @Override
    public Mono<List<String>> listTables(String accessKeyId, String secretAccessKey,
                                         String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    DynamoDbAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return listAllTables(client, null, new ArrayList<>())
                            .map(tables -> {
                                Collections.sort(tables);
                                return tables;
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<DynamoDbTableInfo> describeTable(String accessKeyId, String secretAccessKey,
                                                 String sessionToken, String region,
                                                 String tableName) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(validateTableName(tableName))
                .then(Mono.defer(() -> {
                    DynamoDbAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return Mono.fromFuture(client.describeTable(
                                    DescribeTableRequest.builder().tableName(tableName).build()))
                            .map(resp -> {
                                TableDescription td = resp.table();
                                List<DynamoDbKeySchema> keySchema = td.keySchema().stream()
                                        .map(k -> new DynamoDbKeySchema(k.attributeName(), k.keyTypeAsString()))
                                        .toList();
                                List<DynamoDbAttributeDef> attrDefs = td.attributeDefinitions().stream()
                                        .map(a -> new DynamoDbAttributeDef(a.attributeName(), a.attributeTypeAsString()))
                                        .toList();
                                return new DynamoDbTableInfo(
                                        td.tableName(),
                                        td.tableStatusAsString(),
                                        td.itemCount(),
                                        td.tableSizeBytes(),
                                        td.billingModeSummary() != null
                                                ? td.billingModeSummary().billingModeAsString()
                                                : "PROVISIONED",
                                        keySchema,
                                        attrDefs,
                                        td.globalSecondaryIndexes() != null ? td.globalSecondaryIndexes().size() : 0,
                                        td.localSecondaryIndexes() != null ? td.localSecondaryIndexes().size() : 0,
                                        td.creationDateTime() != null ? td.creationDateTime().toString() : null
                                );
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<DynamoDbScanResult> scanTable(String accessKeyId, String secretAccessKey,
                                              String sessionToken, String region,
                                              String tableName, Integer limit, String lastKey) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(validateTableName(tableName))
                .then(Mono.defer(() -> {
                    DynamoDbAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    int resolvedLimit = resolveLimit(limit);

                    return buildScanRequest(tableName, resolvedLimit, lastKey)
                            .flatMap(scanReq -> Mono.fromFuture(client.scan(scanReq))
                                    .flatMap(resp -> {
                                        List<Map<String, Object>> items = resp.items().stream()
                                                .map(DynamoDbAdapter::attrMapToPlain)
                                                .collect(Collectors.toList());

                                        String nextKey = null;
                                        if (resp.lastEvaluatedKey() != null && !resp.lastEvaluatedKey().isEmpty()) {
                                            try {
                                                Map<String, Object> rawKey = new LinkedHashMap<>();
                                                resp.lastEvaluatedKey().forEach((k, v) -> rawKey.put(k, attrValueToRaw(v)));
                                                nextKey = Base64.getEncoder().encodeToString(MAPPER.writeValueAsBytes(rawKey));
                                            } catch (Exception e) {
                                                log.warn("Error serializing lastEvaluatedKey", e);
                                            }
                                        }

                                        return Mono.just(new DynamoDbScanResult(
                                                items, resp.count(), resp.scannedCount(),
                                                nextKey, nextKey != null
                                        ));
                                    }))
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    // ── DynamoDB attribute conversion ─────────────────────────────────────────

    static Map<String, Object> attrMapToPlain(Map<String, AttributeValue> item) {
        Map<String, Object> out = new LinkedHashMap<>();
        item.forEach((k, v) -> out.put(k, attrValueToPlain(v)));
        return out;
    }

    static Object attrValueToPlain(AttributeValue av) {
        if (av.s() != null) return av.s();
        if (av.n() != null) return av.n();
        if (av.bool() != null) return av.bool();
        if (Boolean.TRUE.equals(av.nul())) return null;
        if (av.b() != null) return "<binary:" + av.b().asByteArray().length + "B>";
        if (av.hasSs()) return av.ss();
        if (av.hasNs()) return av.ns();
        if (av.hasBs()) return "<binary-set>";
        if (av.hasL()) return av.l().stream().map(DynamoDbAdapter::attrValueToPlain).collect(Collectors.toList());
        if (av.hasM()) return attrMapToPlain(av.m());
        return null;
    }

    static Map<String, Object> attrValueToRaw(AttributeValue av) {
        if (av.s() != null) return Map.of("S", av.s());
        if (av.n() != null) return Map.of("N", av.n());
        if (av.bool() != null) return Map.of("BOOL", av.bool());
        if (Boolean.TRUE.equals(av.nul())) return Map.of("NULL", true);
        if (av.b() != null) return Map.of("B", Base64.getEncoder().encodeToString(av.b().asByteArray()));
        if (av.hasSs()) return Map.of("SS", av.ss());
        if (av.hasNs()) return Map.of("NS", av.ns());
        if (av.hasL()) return Map.of("L", av.l().stream().map(DynamoDbAdapter::attrValueToRaw).toList());
        if (av.hasM()) {
            Map<String, Object> m = new LinkedHashMap<>();
            av.m().forEach((k, v) -> m.put(k, attrValueToRaw(v)));
            return Map.of("M", m);
        }
        return Map.of("NULL", true);
    }

    @SuppressWarnings("unchecked")
    static AttributeValue rawToAttributeValue(Map<String, Object> raw) {
        if (raw.containsKey("S")) return AttributeValue.fromS((String) raw.get("S"));
        if (raw.containsKey("N")) return AttributeValue.fromN((String) raw.get("N"));
        if (raw.containsKey("BOOL")) return AttributeValue.fromBool((Boolean) raw.get("BOOL"));
        if (raw.containsKey("NULL")) return AttributeValue.fromNul(true);
        if (raw.containsKey("B")) return AttributeValue.fromB(
                SdkBytes.fromByteArray(Base64.getDecoder().decode((String) raw.get("B"))));
        if (raw.containsKey("SS")) return AttributeValue.fromSs((List<String>) raw.get("SS"));
        if (raw.containsKey("NS")) return AttributeValue.fromNs((List<String>) raw.get("NS"));
        return AttributeValue.fromNul(true);
    }

    // ── Pagination helper ─────────────────────────────────────────────────────

    private Mono<List<String>> listAllTables(DynamoDbAsyncClient client,
                                             String lastTable, List<String> acc) {
        var builder = ListTablesRequest.builder();
        if (lastTable != null) builder.exclusiveStartTableName(lastTable);

        return Mono.fromFuture(client.listTables(builder.build()))
                .flatMap(resp -> {
                    acc.addAll(resp.tableNames());
                    if (resp.lastEvaluatedTableName() != null) {
                        return listAllTables(client, resp.lastEvaluatedTableName(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<ScanRequest> buildScanRequest(String tableName, int limit, String lastKey) {
        ScanRequest.Builder builder = ScanRequest.builder().tableName(tableName).limit(limit);

        if (lastKey != null && !lastKey.isBlank()) {
            try {
                byte[] decoded = Base64.getDecoder().decode(lastKey);
                Map<String, Object> keyMap = MAPPER.readValue(decoded, Map.class);
                Map<String, AttributeValue> startKey = new HashMap<>();
                for (var e : keyMap.entrySet()) {
                    startKey.put(e.getKey(), rawToAttributeValue((Map<String, Object>) e.getValue()));
                }
                builder.exclusiveStartKey(startKey);
            } catch (Exception e) {
                log.warn("Invalid lastKey format, ignoring: {}", e.getMessage());
            }
        }

        return Mono.just(builder.build());
    }

    // ── Client builder ────────────────────────────────────────────────────────

    private DynamoDbAsyncClient buildClient(String region, String accessKeyId,
                                            String secretAccessKey, String sessionToken) {
        var credentials = (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return DynamoDbAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) return DEFAULT_LIMIT;
        return Math.min(MAX_LIMIT, Math.max(1, limit));
    }

    private Mono<Void> validate(String accessKeyId, String secretAccessKey, String region) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new DynamoDbBadRequestException("Falta accessKeyId"));
        }
        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new DynamoDbBadRequestException("Falta secretAccessKey"));
        }
        if (region == null || region.isBlank()) {
            return Mono.error(new DynamoDbBadRequestException("Falta region AWS"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateTableName(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return Mono.error(new DynamoDbBadRequestException("Falta x-dynamodb-table"));
        }
        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof DynamoDbBadRequestException
                || cause instanceof DynamoDbClientException
                || cause instanceof DynamoDbAccessDeniedException) {
            return cause;
        }

        if (cause instanceof ResourceNotFoundException) {
            return new DynamoDbClientException("Tabla DynamoDB no encontrada", cause);
        }

        if (cause instanceof AccessDeniedException) {
            return new DynamoDbAccessDeniedException("Acceso denegado en DynamoDB", cause);
        }

        if (cause instanceof SdkClientException) {
            return new DynamoDbClientException("Error de comunicación con AWS DynamoDB", cause);
        }

        return new DynamoDbClientException("Error inesperado consultando AWS DynamoDB", cause);
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
