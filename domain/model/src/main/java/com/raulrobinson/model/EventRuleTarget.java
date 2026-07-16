package com.raulrobinson.model;

public record EventRuleTarget(
        String id,
        String arn,
        String input,
        String inputPath,
        String roleArn,
        String inputTemplate
) {
}
