package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record S3BucketResponse(String name, String creationDate) {
}
