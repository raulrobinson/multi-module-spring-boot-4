package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record EventBusResponse(String name, String arn, boolean hasPolicy) {
}
