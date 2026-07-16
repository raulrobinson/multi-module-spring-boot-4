package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ApiGatewayApisResponse(List<ApiGatewayApiResponse> apis, List<String> errors) {
}
