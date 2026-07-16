package com.raulrobinson.ports.in;

import com.raulrobinson.model.DynamoDbScanResult;
import com.raulrobinson.model.DynamoDbTableInfo;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IDynamoDbUseCase {

    Mono<List<String>> listTables(String accessKeyId, String secretAccessKey,
                                  String sessionToken, String region);

    Mono<DynamoDbTableInfo> describeTable(String accessKeyId, String secretAccessKey,
                                          String sessionToken, String region,
                                          String tableName);

    Mono<DynamoDbScanResult> scanTable(String accessKeyId, String secretAccessKey,
                                       String sessionToken, String region,
                                       String tableName, Integer limit, String lastKey);
}
