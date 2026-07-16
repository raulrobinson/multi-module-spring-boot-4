package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record S3ObjectsResponse(
        String prefix,
        String bucket,
        List<S3FolderResponse> folders,
        List<S3ObjectItemResponse> objects,
        boolean truncated,
        String nextToken,
        Integer keyCount
) {
}
