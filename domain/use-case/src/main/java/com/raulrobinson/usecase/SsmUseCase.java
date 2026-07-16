package com.raulrobinson.usecase;

import com.raulrobinson.model.SsmParameterValue;
import com.raulrobinson.model.SsmParametersResult;
import com.raulrobinson.ports.in.ISsmUseCase;
import com.raulrobinson.ports.out.SsmGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsmUseCase implements ISsmUseCase {

    private final SsmGateway ssm;

    @Override
    public Mono<SsmParametersResult> listParameters(String accessKeyId, String secretAccessKey,
                                                    String sessionToken, String region) {
        return ssm.listParameters(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<SsmParameterValue> getParameterValue(String accessKeyId, String secretAccessKey,
                                                     String sessionToken, String region,
                                                     String parameterName) {
        return ssm.getParameterValue(accessKeyId, secretAccessKey, sessionToken, region, parameterName);
    }
}
