package com.raulrobinson.ports.out;

import com.raulrobinson.model.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SsoGateway {

    Mono<SsoDeviceAuth> startDeviceAuth(String startUrl, String region);

    Mono<SsoTokenResult> pollToken(String clientId, String clientSecret,
                                   String deviceCode, String region);

    Mono<List<SsoAccount>> listAccounts(String accessToken, String region);

    Mono<List<SsoRole>> listRoles(String accessToken, String accountId, String region);

    Mono<SsoCredentials> getCredentials(String accessToken, String accountId,
                                        String roleName, String region);
}
