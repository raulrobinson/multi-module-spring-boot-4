package com.raulrobinson.driven.jsonplaceholder.mapper;

import com.raulrobinson.driven.jsonplaceholder.dto.PostResponseDto;
import com.raulrobinson.model.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostResponseMapper {

    Post toDomain(PostResponseDto response);
}
