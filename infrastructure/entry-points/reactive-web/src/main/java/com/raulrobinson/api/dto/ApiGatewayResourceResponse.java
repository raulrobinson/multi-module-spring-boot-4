package com.raulrobinson.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ApiGatewayResourceResponse(
        String id,
        String path,
        String pathPart,
        String parentId,
        List<String> methods,
        String routeKey,
        String target,
        String authorizationType
) {
}
