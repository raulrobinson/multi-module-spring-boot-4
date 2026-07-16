package com.raulrobinson.model;

public record SsoDeviceAuth(
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
