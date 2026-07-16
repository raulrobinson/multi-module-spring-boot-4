package com.raulrobinson.model;

public record SsoCredentials(
        String accessKeyId,
        String secretAccessKey,
        String sessionToken,
        String expiration
) {
}
