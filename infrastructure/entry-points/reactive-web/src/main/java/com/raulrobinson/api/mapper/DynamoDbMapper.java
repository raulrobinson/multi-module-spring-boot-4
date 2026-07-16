package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DynamoDbMapper {

    DynamoDbKeySchemaResponse toKeySchemaResponse(DynamoDbKeySchema model);

    DynamoDbAttributeDefResponse toAttributeDefResponse(DynamoDbAttributeDef model);

    DynamoDbTableInfoResponse toTableInfoResponse(DynamoDbTableInfo model);

    DynamoDbScanResponse toScanResponse(DynamoDbScanResult model);
}
