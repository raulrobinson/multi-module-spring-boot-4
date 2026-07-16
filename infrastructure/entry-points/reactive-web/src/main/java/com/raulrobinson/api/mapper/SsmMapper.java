package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SsmMapper {

    SsmParameterResponse toParameterResponse(SsmParameter model);

    SsmParametersResponse toParametersResponse(SsmParametersResult model);

    SsmParameterValueResponse toParameterValueResponse(SsmParameterValue model);
}
