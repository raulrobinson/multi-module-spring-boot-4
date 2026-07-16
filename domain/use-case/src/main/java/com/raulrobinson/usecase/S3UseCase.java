package com.raulrobinson.usecase;

import com.raulrobinson.model.*;
import com.raulrobinson.ports.in.IS3UseCase;
import com.raulrobinson.ports.out.S3Gateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3UseCase implements IS3UseCase {

    private final S3Gateway s3;

    @Override
    public Mono<S3BucketsResult> listBuckets(String accessKeyId, String secretAccessKey,
                                             String sessionToken, String region) {
        return s3.listBuckets(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<S3ObjectsResult> listObjects(String accessKeyId, String secretAccessKey,
                                             String sessionToken, String region,
                                             String bucket, String prefix, String continuationToken) {
        return s3.listObjects(accessKeyId, secretAccessKey, sessionToken, region, bucket, prefix, continuationToken);
    }

    @Override
    public Mono<S3ObjectInfo> getObjectInfo(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region,
                                            String bucket, String key) {
        return s3.getObjectInfo(accessKeyId, secretAccessKey, sessionToken, region, bucket, key);
    }

    @Override
    public Mono<S3ObjectContent> getObjectContent(String accessKeyId, String secretAccessKey,
                                                  String sessionToken, String region,
                                                  String bucket, String key) {
        return s3.getObjectContent(accessKeyId, secretAccessKey, sessionToken, region, bucket, key);
    }

    @Override
    public Mono<S3PresignResult> presignObject(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region,
                                               String bucket, String key) {
        return s3.presignObject(accessKeyId, secretAccessKey, sessionToken, region, bucket, key);
    }
}
