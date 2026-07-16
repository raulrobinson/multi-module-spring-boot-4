package com.raulrobinson.model;

import java.util.List;

public record SecretValue(
        String name,
        String arn,
        String secretString,
        String versionId,
        List<String> versionStages,
        String createdDate
) {
}
