package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record EventPutEntryResponse(
        String eventId,
        String errorCode,
        String errorMessage,
        boolean success
) {
}
