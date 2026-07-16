package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface S3Mapper {

    S3BucketResponse toBucketResponse(S3Bucket model);

    S3BucketsResponse toBucketsResponse(S3BucketsResult model);

    S3FolderResponse toFolderResponse(S3Folder model);

    S3ObjectItemResponse toObjectItemResponse(S3ObjectItem model);

    S3ObjectsResponse toObjectsResponse(S3ObjectsResult model);

    S3ObjectInfoResponse toObjectInfoResponse(S3ObjectInfo model);

    S3ObjectContentResponse toObjectContentResponse(S3ObjectContent model);

    S3PresignResponse toPresignResponse(S3PresignResult model);
}
