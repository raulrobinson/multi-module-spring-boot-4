package com.raulrobinson.model;

import java.util.List;

public record EventRuleDetail(
        String name,
        String arn,
        String state,
        String description,
        String scheduleExpression,
        String eventPattern,
        String eventBusName,
        String managedBy,
        String roleArn,
        List<EventRuleTarget> targets
) {
}
