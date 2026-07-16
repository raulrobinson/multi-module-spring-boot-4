package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsmParameterValueResponse(
        String name,
        String type,
        String value,
        Long version,
        String lastModifiedDate,
        String arn,
        String dataType
) {
}
