package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record S3BucketsResponse(List<S3BucketResponse> buckets) {
}
