package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record S3ObjectContentResponse(
        Long size,
        String contentType,
        boolean viewable,
        String reason,
        String mediaKind,
        String encoding,
        String content
) {
}
