package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record S3ObjectItemResponse(
        String key,
        Long size,
        String lastModified,
        String etag,
        String storageClass
) {
}
