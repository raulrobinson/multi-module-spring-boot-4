package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsmParameterResponse(
        String name,
        String type,
        String description,
        String lastModifiedDate,
        Long version,
        String tier,
        String dataType
) {
}
