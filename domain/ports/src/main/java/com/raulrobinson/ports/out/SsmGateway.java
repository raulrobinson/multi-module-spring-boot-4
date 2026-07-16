package com.raulrobinson.ports.out;

import com.raulrobinson.model.SsmParameterValue;
import com.raulrobinson.model.SsmParametersResult;
import reactor.core.publisher.Mono;

public interface SsmGateway {

    Mono<SsmParametersResult> listParameters(String accessKeyId, String secretAccessKey,
                                             String sessionToken, String region);

    Mono<SsmParameterValue> getParameterValue(String accessKeyId, String secretAccessKey,
                                              String sessionToken, String region,
                                              String parameterName);
}
