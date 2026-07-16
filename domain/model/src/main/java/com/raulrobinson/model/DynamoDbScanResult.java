package com.raulrobinson.model;

import java.util.List;
import java.util.Map;

public record DynamoDbScanResult(
        List<Map<String, Object>> items,
        Integer count,
        Integer scannedCount,
        String lastKey,
        boolean hasMore
) {
}
