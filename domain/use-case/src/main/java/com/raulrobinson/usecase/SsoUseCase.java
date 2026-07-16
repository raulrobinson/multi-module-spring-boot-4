package com.raulrobinson.usecase;

import com.raulrobinson.model.*;
import com.raulrobinson.ports.in.ISsoUseCase;
import com.raulrobinson.ports.out.SsoGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsoUseCase implements ISsoUseCase {

    private final SsoGateway sso;

    @Override
    public Mono<SsoDeviceAuth> startDeviceAuth(String startUrl, String region) {
        return sso.startDeviceAuth(startUrl, region);
    }

    @Override
    public Mono<SsoTokenResult> pollToken(String clientId, String clientSecret,
                                          String deviceCode, String region) {
        return sso.pollToken(clientId, clientSecret, deviceCode, region);
    }

    @Override
    public Mono<List<SsoAccount>> listAccounts(String accessToken, String region) {
        return sso.listAccounts(accessToken, region);
    }

    @Override
    public Mono<List<SsoRole>> listRoles(String accessToken, String accountId, String region) {
        return sso.listRoles(accessToken, accountId, region);
    }

    @Override
    public Mono<SsoCredentials> getCredentials(String accessToken, String accountId,
                                               String roleName, String region) {
        return sso.getCredentials(accessToken, accountId, roleName, region);
    }
}
