package com.raulrobinson.api.handlers;

import com.raulrobinson.api.dto.DynamoDbTablesResponse;
import com.raulrobinson.api.mapper.DynamoDbMapper;
import com.raulrobinson.exception.DynamoDbBadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.IDynamoDbUseCase;
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
public class DynamoDbHandler extends RequestValidation {

    private final IDynamoDbUseCase dynamodb;
    private final DynamoDbMapper mapper;

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT     = 200;

    // ── POST /api/dynamodb/tables ─────────────────────────────────────────────
    public Mono<ServerResponse> listTables(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new DynamoDbBadRequestException("Faltan credenciales AWS"));
        }

        return dynamodb.listTables(accessKeyId, secretAccessKey, sessionToken, region)
                .map(tables -> DynamoDbTablesResponse.builder().tables(tables).build())
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/dynamodb/table-info ─────────────────────────────────────────
    public Mono<ServerResponse> describeTable(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String tableName       = header(req, "x-dynamodb-table");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new DynamoDbBadRequestException("Faltan credenciales AWS"));
        }
        if (tableName == null || tableName.isBlank()) {
            return Mono.error(new DynamoDbBadRequestException("Falta header x-dynamodb-table"));
        }

        return dynamodb.describeTable(accessKeyId, secretAccessKey, sessionToken, region, tableName)
                .map(mapper::toTableInfoResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/dynamodb/scan ───────────────────────────────────────────────
    public Mono<ServerResponse> scanTable(ServerRequest req) {
        String accessKeyId     = header(req, "x-aws-access-key-id");
        String secretAccessKey = header(req, "x-aws-secret-access-key");
        String sessionToken    = header(req, "x-aws-session-token");
        String region          = blankFallback(header(req, "x-aws-region"), "us-east-1");
        String tableName       = header(req, "x-dynamodb-table");
        String limitStr        = header(req, "x-dynamodb-limit");
        String lastKey         = header(req, "x-dynamodb-last-key");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new DynamoDbBadRequestException("Faltan credenciales AWS"));
        }
        if (tableName == null || tableName.isBlank()) {
            return Mono.error(new DynamoDbBadRequestException("Falta header x-dynamodb-table"));
        }

        int limit = DEFAULT_LIMIT;
        if (limitStr != null && !limitStr.isBlank()) {
            try { limit = Math.min(MAX_LIMIT, Integer.parseInt(limitStr)); }
            catch (NumberFormatException ignored) {}
        }

        return dynamodb.scanTable(accessKeyId, secretAccessKey, sessionToken, region, tableName, limit, lastKey)
                .map(mapper::toScanResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
