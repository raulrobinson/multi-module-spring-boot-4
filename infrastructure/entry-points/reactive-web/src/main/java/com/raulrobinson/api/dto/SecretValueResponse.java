package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SecretValueResponse(
        String name,
        String arn,
        String secretString,
        String versionId,
        List<String> versionStages,
        String createdDate
) {
}
