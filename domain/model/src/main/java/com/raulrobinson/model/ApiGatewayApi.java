package com.raulrobinson.model;

public record ApiGatewayApi(
        String id,
        String name,
        String type,
        String description,
        String createdDate,
        String apiEndpoint,
        String endpointType
) {
}
