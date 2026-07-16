package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record S3FolderResponse(String prefix) {
}
