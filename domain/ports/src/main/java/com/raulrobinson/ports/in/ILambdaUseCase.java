package com.raulrobinson.ports.in;

import com.raulrobinson.model.LambdaFunctionsResult;
import com.raulrobinson.model.LambdaInvokeResult;
import reactor.core.publisher.Mono;

public interface ILambdaUseCase {

    Mono<LambdaInvokeResult> invokeFunction(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region,
                                            String functionArn, String payload);

    Mono<LambdaFunctionsResult> listFunctions(String accessKeyId, String secretAccessKey,
                                              String sessionToken, String region);
}
