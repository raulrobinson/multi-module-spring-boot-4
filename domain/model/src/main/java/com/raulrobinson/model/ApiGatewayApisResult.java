package com.raulrobinson.model;

import java.util.List;

public record ApiGatewayApisResult(
        List<ApiGatewayApi> apis,
        List<String> errors
) {
}
