package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record ApiGatewayKeyResponse(
        String id,
        String name,
        String value,
        boolean enabled,
        String description
) {
}
