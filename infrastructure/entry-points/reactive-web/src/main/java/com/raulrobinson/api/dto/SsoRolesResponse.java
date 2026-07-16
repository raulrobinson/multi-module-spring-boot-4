package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SsoRolesResponse(List<SsoRoleResponse> roles) {
}
