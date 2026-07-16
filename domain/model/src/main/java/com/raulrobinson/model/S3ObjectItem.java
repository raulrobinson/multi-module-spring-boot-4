package com.raulrobinson.model;

public record S3ObjectItem(
        String key,
        Long size,
        String lastModified,
        String etag,
        String storageClass
) {
}
