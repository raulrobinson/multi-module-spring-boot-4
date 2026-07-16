package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record DynamoDbAttributeDefResponse(String attributeName, String attributeType) {
}
