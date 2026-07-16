package com.raulrobinson.ports.in;

import com.raulrobinson.model.IamRolesResult;
import com.raulrobinson.model.IamUsersResult;
import reactor.core.publisher.Mono;

public interface IIamUseCase {

    Mono<IamUsersResult> listUsers(String accessKeyId, String secretAccessKey,
                                   String sessionToken, String region);

    Mono<IamRolesResult> listRoles(String accessKeyId, String secretAccessKey,
                                   String sessionToken, String region);
}
