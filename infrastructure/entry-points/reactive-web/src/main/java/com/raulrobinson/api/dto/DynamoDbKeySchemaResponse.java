package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record DynamoDbKeySchemaResponse(String attributeName, String keyType) {
}
