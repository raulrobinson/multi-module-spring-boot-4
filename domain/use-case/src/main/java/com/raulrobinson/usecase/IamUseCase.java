package com.raulrobinson.usecase;

import com.raulrobinson.model.IamRolesResult;
import com.raulrobinson.model.IamUsersResult;
import com.raulrobinson.ports.in.IIamUseCase;
import com.raulrobinson.ports.out.IamGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IamUseCase implements IIamUseCase {

    private final IamGateway iam;

    @Override
    public Mono<IamUsersResult> listUsers(String accessKeyId, String secretAccessKey,
                                          String sessionToken, String region) {
        return iam.listUsers(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<IamRolesResult> listRoles(String accessKeyId, String secretAccessKey,
                                          String sessionToken, String region) {
        return iam.listRoles(accessKeyId, secretAccessKey, sessionToken, region);
    }
}
