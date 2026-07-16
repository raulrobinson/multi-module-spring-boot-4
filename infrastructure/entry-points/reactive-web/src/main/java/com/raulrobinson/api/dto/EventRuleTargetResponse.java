package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record EventRuleTargetResponse(
        String id,
        String arn,
        String input,
        String inputPath,
        String roleArn,
        String inputTemplate
) {
}
