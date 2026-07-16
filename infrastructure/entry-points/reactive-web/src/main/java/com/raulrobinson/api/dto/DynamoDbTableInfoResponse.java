package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DynamoDbTableInfoResponse(
        String tableName,
        String status,
        Long itemCount,
        Long tableSizeBytes,
        String billingMode,
        List<DynamoDbKeySchemaResponse> keySchema,
        List<DynamoDbAttributeDefResponse> attributeDefinitions,
        Integer gsiCount,
        Integer lsiCount,
        String creationDateTime
) {
}
