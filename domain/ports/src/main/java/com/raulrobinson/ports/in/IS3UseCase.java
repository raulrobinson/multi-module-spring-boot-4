package com.raulrobinson.ports.in;

import com.raulrobinson.model.*;
import reactor.core.publisher.Mono;

public interface IS3UseCase {

    Mono<S3BucketsResult> listBuckets(String accessKeyId, String secretAccessKey,
                                      String sessionToken, String region);

    Mono<S3ObjectsResult> listObjects(String accessKeyId, String secretAccessKey,
                                      String sessionToken, String region,
                                      String bucket, String prefix, String continuationToken);

    Mono<S3ObjectInfo> getObjectInfo(String accessKeyId, String secretAccessKey,
                                     String sessionToken, String region,
                                     String bucket, String key);

    Mono<S3ObjectContent> getObjectContent(String accessKeyId, String secretAccessKey,
                                           String sessionToken, String region,
                                           String bucket, String key);

    Mono<S3PresignResult> presignObject(String accessKeyId, String secretAccessKey,
                                        String sessionToken, String region,
                                        String bucket, String key);
}
