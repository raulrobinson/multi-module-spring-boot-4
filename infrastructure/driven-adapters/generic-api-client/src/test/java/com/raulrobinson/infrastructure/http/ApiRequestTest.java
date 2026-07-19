package com.raulrobinson.infrastructure.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import com.raulrobinson.infrastructure.http.dto.ApiRequest;
import org.junit.jupiter.api.Test;

class ApiRequestTest {
    @Test
    void createsDefensiveCopiesAndCorrelationId() {
        var headers = new HashMap<>(Map.of("Authorization", "Bearer token"));
        var request = ApiRequest.builder()
                .operation("retrieve-account")
                .headers(headers)
                .build();

        headers.clear();

        assertThat(request.headers()).containsEntry("Authorization", "Bearer token");
        assertThat(request.correlationId()).isNotBlank();
        assertThatThrownBy(() -> request.headers().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsBlankOperation() {
        assertThatThrownBy(() -> ApiRequest.builder().operation(" ").build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
