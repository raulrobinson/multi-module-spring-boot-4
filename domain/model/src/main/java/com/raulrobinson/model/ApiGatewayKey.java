package com.raulrobinson.model;

public record ApiGatewayKey(
        String id,
        String name,
        String value,
        boolean enabled,
        String description
) {
}
