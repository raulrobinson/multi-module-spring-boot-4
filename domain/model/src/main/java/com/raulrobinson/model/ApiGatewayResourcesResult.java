package com.raulrobinson.model;

import java.util.List;

public record ApiGatewayResourcesResult(
        String apiType,
        List<ApiGatewayResource> items
) {
}
