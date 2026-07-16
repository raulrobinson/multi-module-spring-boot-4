package com.raulrobinson.model;

import java.util.List;

public record S3ObjectsResult(
        String prefix,
        String bucket,
        List<S3Folder> folders,
        List<S3ObjectItem> objects,
        boolean truncated,
        String nextToken,
        Integer keyCount
) {
}
