package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record EventRuleResponse(
        String name,
        String arn,
        String state,
        String description,
        String scheduleExpression,
        boolean hasEventPattern,
        String managedBy
) {
}
