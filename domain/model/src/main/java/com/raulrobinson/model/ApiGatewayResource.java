package com.raulrobinson.model;

import java.util.List;

public record ApiGatewayResource(
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
