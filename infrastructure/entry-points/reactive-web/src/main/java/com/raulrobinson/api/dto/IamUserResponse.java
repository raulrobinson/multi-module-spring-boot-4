package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record IamUserResponse(
        String userName,
        String arn,
        String path,
        String createDate
) {
}
