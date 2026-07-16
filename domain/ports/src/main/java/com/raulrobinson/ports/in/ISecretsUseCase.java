package com.raulrobinson.ports.in;

import com.raulrobinson.model.Secret;
import com.raulrobinson.model.SecretValue;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ISecretsUseCase {
    Mono<List<Secret>> listSecrets(String accessKeyId,
                                   String secretAccessKey,
                                   String sessionToken,
                                   String region);

    Mono<SecretValue> getSecretValue(String accessKeyId,
                                     String secretAccessKey,
                                     String sessionToken,
                                     String region,
                                     String secretId);
}
