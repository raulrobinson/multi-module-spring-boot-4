package com.raulrobinson.usecase;

import com.raulrobinson.model.DynamoDbScanResult;
import com.raulrobinson.model.DynamoDbTableInfo;
import com.raulrobinson.ports.in.IDynamoDbUseCase;
import com.raulrobinson.ports.out.DynamoDbGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamoDbUseCase implements IDynamoDbUseCase {

    private final DynamoDbGateway dynamoDb;

    @Override
    public Mono<List<String>> listTables(String accessKeyId, String secretAccessKey,
                                         String sessionToken, String region) {
        return dynamoDb.listTables(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<DynamoDbTableInfo> describeTable(String accessKeyId, String secretAccessKey,
                                                 String sessionToken, String region,
                                                 String tableName) {
        return dynamoDb.describeTable(accessKeyId, secretAccessKey, sessionToken, region, tableName);
    }

    @Override
    public Mono<DynamoDbScanResult> scanTable(String accessKeyId, String secretAccessKey,
                                              String sessionToken, String region,
                                              String tableName, Integer limit, String lastKey) {
        return dynamoDb.scanTable(accessKeyId, secretAccessKey, sessionToken, region, tableName, limit, lastKey);
    }
}
