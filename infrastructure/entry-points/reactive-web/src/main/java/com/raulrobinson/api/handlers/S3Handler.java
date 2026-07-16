package com.raulrobinson.api.handlers;

import com.raulrobinson.api.mapper.S3Mapper;
import com.raulrobinson.exception.S3BadRequestException;
import com.raulrobinson.helper.RequestValidation;
import com.raulrobinson.ports.in.IS3UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Handler extends RequestValidation {

    private final IS3UseCase s3;
    private final S3Mapper mapper;

    // ── POST /api/s3/buckets ──────────────────────────────────────────────────
    public Mono<ServerResponse> listBuckets(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new S3BadRequestException("Faltan credenciales AWS"));
        }

        return s3.listBuckets(accessKeyId, secretAccessKey, sessionToken, region)
                .map(mapper::toBucketsResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/s3/objects ──────────────────────────────────────────────────
    public Mono<ServerResponse> listObjects(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String bucket          = header(request, "x-s3-bucket");
        String prefix          = blankFallback(header(request, "x-s3-prefix"), "");
        String contToken       = header(request, "x-s3-continuation-token");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new S3BadRequestException("Faltan credenciales AWS"));
        }
        if (bucket == null || bucket.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-bucket"));
        }

        return s3.listObjects(accessKeyId, secretAccessKey, sessionToken, region, bucket, prefix, contToken)
                .map(mapper::toObjectsResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/s3/object-info ──────────────────────────────────────────────
    public Mono<ServerResponse> objectInfo(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String bucket          = header(request, "x-s3-bucket");
        String key             = header(request, "x-s3-key");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new S3BadRequestException("Faltan credenciales AWS"));
        }
        if (bucket == null || bucket.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-bucket"));
        }
        if (key == null || key.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-key"));
        }

        return s3.getObjectInfo(accessKeyId, secretAccessKey, sessionToken, region, bucket, key)
                .map(mapper::toObjectInfoResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/s3/content ──────────────────────────────────────────────────
    public Mono<ServerResponse> getObjectContent(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String bucket          = header(request, "x-s3-bucket");
        String key             = header(request, "x-s3-key");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new S3BadRequestException("Faltan credenciales AWS"));
        }
        if (bucket == null || bucket.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-bucket"));
        }
        if (key == null || key.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-key"));
        }

        return s3.getObjectContent(accessKeyId, secretAccessKey, sessionToken, region, bucket, key)
                .map(mapper::toObjectContentResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    // ── POST /api/s3/presign ──────────────────────────────────────────────────
    public Mono<ServerResponse> presignObject(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String bucket          = header(request, "x-s3-bucket");
        String key             = header(request, "x-s3-key");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new S3BadRequestException("Faltan credenciales AWS"));
        }
        if (bucket == null || bucket.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-bucket"));
        }
        if (key == null || key.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta header x-s3-key"));
        }

        return s3.presignObject(accessKeyId, secretAccessKey, sessionToken, region, bucket, key)
                .map(mapper::toPresignResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
