package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.SecretResponse;
import com.raulrobinson.api.dto.SecretValueResponse;
import com.raulrobinson.api.dto.SecretsResponse;
import com.raulrobinson.model.Secret;
import com.raulrobinson.model.SecretValue;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SecretsMapper {

    SecretResponse toSecretResponse(Secret secret);

    SecretValueResponse toSecretValueResponse(SecretValue secretValue);

    default SecretsResponse toSecretsResponse(List<Secret> secrets) {
        return SecretsResponse.builder()
                .secrets(secrets.stream().map(this::toSecretResponse).toList())
                .build();
    }
}
