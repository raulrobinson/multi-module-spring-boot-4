package com.raulrobinson.model;

import java.util.List;

public record ApiGatewayKeysResult(
        List<ApiGatewayKey> keys,
        String note,
        String error
) {
}
