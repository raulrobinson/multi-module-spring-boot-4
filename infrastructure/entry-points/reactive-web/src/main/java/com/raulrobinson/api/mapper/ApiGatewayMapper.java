package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiGatewayMapper {

    ApiGatewayApiResponse toApiResponse(ApiGatewayApi model);

    ApiGatewayApisResponse toApisResponse(ApiGatewayApisResult model);

    ApiGatewayStageResponse toStageResponse(ApiGatewayStage model);

    ApiGatewayStagesResponse toStagesResponse(ApiGatewayStagesResult model);

    ApiGatewayResourceResponse toResourceResponse(ApiGatewayResource model);

    ApiGatewayResourcesResponse toResourcesResponse(ApiGatewayResourcesResult model);

    ApiGatewayKeyResponse toKeyResponse(ApiGatewayKey model);

    ApiGatewayKeysResponse toKeysResponse(ApiGatewayKeysResult model);
}
