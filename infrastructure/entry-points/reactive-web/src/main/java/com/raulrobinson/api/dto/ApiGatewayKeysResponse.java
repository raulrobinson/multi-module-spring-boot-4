package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ApiGatewayKeysResponse(List<ApiGatewayKeyResponse> keys, String note, String error) {
}
