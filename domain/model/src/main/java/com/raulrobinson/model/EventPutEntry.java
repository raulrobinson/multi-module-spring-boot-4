package com.raulrobinson.model;

public record EventPutEntry(
        String eventId,
        String errorCode,
        String errorMessage,
        boolean success
) {
}
