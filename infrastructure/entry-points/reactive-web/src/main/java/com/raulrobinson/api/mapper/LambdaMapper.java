package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LambdaMapper {

    LambdaFunctionResponse toFunctionResponse(LambdaFunction model);

    LambdaFunctionsResponse toFunctionsResponse(LambdaFunctionsResult model);

    LambdaInvokeResponse toInvokeResponse(LambdaInvokeResult model);
}
