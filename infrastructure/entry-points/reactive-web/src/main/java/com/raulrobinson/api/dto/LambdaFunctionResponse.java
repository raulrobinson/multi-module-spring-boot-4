package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record LambdaFunctionResponse(String fnName, String arn) {
}
