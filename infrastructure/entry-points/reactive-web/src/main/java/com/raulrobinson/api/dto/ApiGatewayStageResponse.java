package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record ApiGatewayStageResponse(
        String name,
        String description,
        String deploymentId,
        String lastUpdated,
        String createdDate,
        String stageUrl,
        Double throttlingRateLimit,
        Double throttlingBurstLimit
) {
}
