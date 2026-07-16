package com.raulrobinson.usecase;

import com.raulrobinson.model.LambdaFunctionsResult;
import com.raulrobinson.model.LambdaInvokeResult;
import com.raulrobinson.ports.in.ILambdaUseCase;
import com.raulrobinson.ports.out.LambdaGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LambdaUseCase implements ILambdaUseCase {

    private final LambdaGateway lambda;

    @Override
    public Mono<LambdaInvokeResult> invokeFunction(String accessKeyId, String secretAccessKey,
                                                   String sessionToken, String region,
                                                   String functionArn, String payload) {
        return lambda.invokeFunction(accessKeyId, secretAccessKey, sessionToken, region, functionArn, payload);
    }

    @Override
    public Mono<LambdaFunctionsResult> listFunctions(String accessKeyId, String secretAccessKey,
                                                     String sessionToken, String region) {
        return lambda.listFunctions(accessKeyId, secretAccessKey, sessionToken, region);
    }
}
