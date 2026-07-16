package com.raulrobinson.model;

public record SsmParameter(
        String name,
        String type,
        String description,
        String lastModifiedDate,
        Long version,
        String tier,
        String dataType
) {
}
