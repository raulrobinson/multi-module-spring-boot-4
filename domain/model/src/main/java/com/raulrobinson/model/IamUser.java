package com.raulrobinson.model;

public record IamUser(
        String userName,
        String arn,
        String path,
        String createDate
) {
}
