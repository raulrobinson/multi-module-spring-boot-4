package com.raulrobinson.usecase;

import com.raulrobinson.model.Secret;
import com.raulrobinson.model.SecretValue;
import com.raulrobinson.ports.in.ISecretsUseCase;
import com.raulrobinson.ports.out.SecretsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecretsUseCase implements ISecretsUseCase {

    private final SecretsGateway secrets;

    @Override
    public Mono<List<Secret>> listSecrets(String accessKeyId, String secretAccessKey, String sessionToken, String region) {
        return secrets.listSecrets(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<SecretValue> getSecretValue(String accessKeyId, String secretAccessKey, String sessionToken, String region, String secretId) {
        return secrets.getSecretValue(accessKeyId, secretAccessKey, sessionToken, region, secretId);
    }
}
