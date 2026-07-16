package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.IamRoleResponse;
import com.raulrobinson.api.dto.IamRolesResponse;
import com.raulrobinson.api.dto.IamUserResponse;
import com.raulrobinson.api.dto.IamUsersResponse;
import com.raulrobinson.model.IamRole;
import com.raulrobinson.model.IamRolesResult;
import com.raulrobinson.model.IamUser;
import com.raulrobinson.model.IamUsersResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IamMapper {

    IamUserResponse toUserResponse(IamUser model);

    IamUsersResponse toUsersResponse(IamUsersResult model);

    IamRoleResponse toRoleResponse(IamRole model);

    IamRolesResponse toRolesResponse(IamRolesResult model);
}
