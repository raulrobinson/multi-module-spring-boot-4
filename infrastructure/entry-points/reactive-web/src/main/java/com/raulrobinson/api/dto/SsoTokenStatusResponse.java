package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsoTokenStatusResponse(String status, String accessToken) {
}
