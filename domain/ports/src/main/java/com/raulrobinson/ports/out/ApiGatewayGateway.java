package com.raulrobinson.ports.out;

import com.raulrobinson.model.*;
import reactor.core.publisher.Mono;

public interface ApiGatewayGateway {

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
