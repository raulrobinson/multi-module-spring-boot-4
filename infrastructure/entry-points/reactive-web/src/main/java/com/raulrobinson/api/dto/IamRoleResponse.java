package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record IamRoleResponse(
        String roleName,
        String arn,
        String path,
        String createDate,
        boolean irsaEnabled,
        List<String> oidcProviders
) {
}
