package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record S3ObjectInfoResponse(
        String key,
        String bucket,
        Long contentLength,
        String contentType,
        String lastModified,
        String etag,
        String storageClass,
        Map<String, String> metadata,
        String versionId
) {
}
