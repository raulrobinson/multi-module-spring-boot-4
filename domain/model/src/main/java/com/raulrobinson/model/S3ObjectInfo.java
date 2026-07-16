package com.raulrobinson.model;

import java.util.Map;

public record S3ObjectInfo(
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
