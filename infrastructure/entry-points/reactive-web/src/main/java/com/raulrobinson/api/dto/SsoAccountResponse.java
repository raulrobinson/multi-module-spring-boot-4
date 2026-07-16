package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsoAccountResponse(String accountId, String accountName, String emailAddress) {
}
