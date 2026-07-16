package com.raulrobinson.model;

import java.util.List;

public record DynamoDbTableInfo(
        String tableName,
        String status,
        Long itemCount,
        Long tableSizeBytes,
        String billingMode,
        List<DynamoDbKeySchema> keySchema,
        List<DynamoDbAttributeDef> attributeDefinitions,
        Integer gsiCount,
        Integer lsiCount,
        String creationDateTime
) {
}
