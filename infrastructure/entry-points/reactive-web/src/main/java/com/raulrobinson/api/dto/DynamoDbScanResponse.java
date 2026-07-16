package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record DynamoDbScanResponse(
        List<Map<String, Object>> items,
        Integer count,
        Integer scannedCount,
        String lastKey,
        boolean hasMore
) {
}
