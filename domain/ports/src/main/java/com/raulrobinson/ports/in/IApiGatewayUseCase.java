package com.raulrobinson.ports.in;

import com.raulrobinson.model.*;
import reactor.core.publisher.Mono;

public interface IApiGatewayUseCase {

    Mono<ApiGatewayApisResult> listApis(String accessKeyId, String secretAccessKey,
                                        String sessionToken, String region);

    Mono<ApiGatewayStagesResult> listStages(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region,
                                            String apiId, String apiType);

    Mono<ApiGatewayResourcesResult> listResources(String accessKeyId, String secretAccessKey,
                                                  String sessionToken, String region,
                                                  String apiId, String apiType);

    Mono<ApiGatewayKeysResult> listKeys(String accessKeyId, String secretAccessKey,
                                        String sessionToken, String region,
                                        String apiId, String apiType);
}
