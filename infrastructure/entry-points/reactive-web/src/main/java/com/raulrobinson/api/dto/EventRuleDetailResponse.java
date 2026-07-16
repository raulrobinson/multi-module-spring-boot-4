package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record EventRuleDetailResponse(
        String name,
        String arn,
        String state,
        String description,
        String scheduleExpression,
        String eventPattern,
        String eventBusName,
        String managedBy,
        String roleArn,
        List<EventRuleTargetResponse> targets
) {
}
