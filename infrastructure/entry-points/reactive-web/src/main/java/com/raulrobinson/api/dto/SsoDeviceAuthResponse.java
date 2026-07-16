package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsoDeviceAuthResponse(
        String clientId,
        String clientSecret,
        String deviceCode,
        String userCode,
        String verificationUri,
        String verificationUriComplete,
        Integer expiresIn,
        Integer interval
) {
}
