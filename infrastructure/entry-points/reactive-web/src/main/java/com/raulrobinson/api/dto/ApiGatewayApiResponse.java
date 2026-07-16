package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record ApiGatewayApiResponse(
        String id,
        String name,
        String type,
        String description,
        String createdDate,
        String apiEndpoint,
        String endpointType
) {
}
