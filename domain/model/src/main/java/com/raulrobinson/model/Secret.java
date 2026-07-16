package com.raulrobinson.model;

public record Secret(
        String name,
        String arn,
        String description,
        String lastChangedDate,
        String lastAccessedDate,
        String createdDate,
        String deletedDate
) {
}