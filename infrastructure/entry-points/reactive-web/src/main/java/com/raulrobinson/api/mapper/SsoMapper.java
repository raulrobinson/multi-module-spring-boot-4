package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SsoMapper {

    SsoDeviceAuthResponse toDeviceAuthResponse(SsoDeviceAuth model);

    SsoTokenStatusResponse toTokenStatusResponse(SsoTokenResult model);

    SsoAccountResponse toAccountResponse(SsoAccount model);

    SsoRoleResponse toRoleResponse(SsoRole model);

    SsoCredentialsResponse toCredentialsResponse(SsoCredentials model);
}
