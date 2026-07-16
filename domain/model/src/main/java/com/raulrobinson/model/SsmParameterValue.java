package com.raulrobinson.model;

public record SsmParameterValue(
        String name,
        String type,
        String value,
        Long version,
        String lastModifiedDate,
        String arn,
        String dataType
) {
}
