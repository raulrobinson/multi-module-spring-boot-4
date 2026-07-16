package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SsmParametersResponse(List<SsmParameterResponse> parameters) {
}
