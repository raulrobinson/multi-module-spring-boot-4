package com.raulrobinson.usecase;

import com.raulrobinson.model.*;
import com.raulrobinson.ports.in.IApiGatewayUseCase;
import com.raulrobinson.ports.out.ApiGatewayGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiGatewayUseCase implements IApiGatewayUseCase {

    private final ApiGatewayGateway apiGateway;

    @Override
    public Mono<ApiGatewayApisResult> listApis(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region) {
        return apiGateway.listApis(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<ApiGatewayStagesResult> listStages(String accessKeyId, String secretAccessKey,
                                                   String sessionToken, String region,
                                                   String apiId, String apiType) {
        return apiGateway.listStages(accessKeyId, secretAccessKey, sessionToken, region, apiId, apiType);
    }

    @Override
    public Mono<ApiGatewayResourcesResult> listResources(String accessKeyId, String secretAccessKey,
                                                         String sessionToken, String region,
                                                         String apiId, String apiType) {
        return apiGateway.listResources(accessKeyId, secretAccessKey, sessionToken, region, apiId, apiType);
    }

    @Override
    public Mono<ApiGatewayKeysResult> listKeys(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region,
                                               String apiId, String apiType) {
        return apiGateway.listKeys(accessKeyId, secretAccessKey, sessionToken, region, apiId, apiType);
    }
}
