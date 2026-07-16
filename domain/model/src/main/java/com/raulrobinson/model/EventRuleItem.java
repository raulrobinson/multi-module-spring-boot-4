package com.raulrobinson.model;

public record EventRuleItem(
        String name,
        String arn,
        String state,
        String description,
        String scheduleExpression,
        boolean hasEventPattern,
        String managedBy
) {
}
