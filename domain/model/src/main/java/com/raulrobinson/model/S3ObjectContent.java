package com.raulrobinson.model;

public record S3ObjectContent(
        Long size,
        String contentType,
        boolean viewable,
        String reason,
        String mediaKind,
        String encoding,
        String content
) {
}
