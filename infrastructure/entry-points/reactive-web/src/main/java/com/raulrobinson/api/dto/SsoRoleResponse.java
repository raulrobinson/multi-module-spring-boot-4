package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record SsoRoleResponse(String roleName, String accountId) {
}
