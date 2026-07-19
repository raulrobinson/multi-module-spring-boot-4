package com.raulrobinson.driven.jsonplaceholder.dto;

public record PostResponseDto(
        Long userId,
        Long id,
        String title,
        String body
) {
}
