package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.MultiRequestDto;
import com.raulrobinson.model.MultiModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MultiApiMapper {

    MultiModel toModel(MultiRequestDto dto);
}
