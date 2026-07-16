package com.raulrobinson.model;

public record ApiGatewayStage(
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
