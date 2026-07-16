package com.raulrobinson.model;

import java.util.List;

public record IamRole(
        String roleName,
        String arn,
        String path,
        String createDate,
        boolean irsaEnabled,
        List<String> oidcProviders
) {
}
