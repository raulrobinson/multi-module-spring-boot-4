package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SecretResponse(
        String name,
        String arn,
        String description,
        String lastChangedDate,
        String lastAccessedDate,
        String createdDate,
        String deletedDate
) {
}
