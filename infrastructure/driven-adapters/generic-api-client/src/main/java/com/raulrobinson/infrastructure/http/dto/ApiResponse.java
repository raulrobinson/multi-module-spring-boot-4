package com.raulrobinson.infrastructure.http.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ApiResponse<T>(int status, Map<String, List<String>> headers, T body) {
    public ApiResponse {
        var copy = new LinkedHashMap<String, List<String>>();
        if (headers != null) {
            headers.forEach((key, values) -> copy.put(key, List.copyOf(values)));
        }
        headers = Map.copyOf(copy);
    }

    public boolean isSuccessful() { return status >= 200 && status < 300; }
}
