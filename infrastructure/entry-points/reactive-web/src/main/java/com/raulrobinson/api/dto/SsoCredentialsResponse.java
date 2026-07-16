package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsoCredentialsResponse(
        String accessKeyId,
        String secretAccessKey,
        String sessionToken,
        String expiration
) {
}
