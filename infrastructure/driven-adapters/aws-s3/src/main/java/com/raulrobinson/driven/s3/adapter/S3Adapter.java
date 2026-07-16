package com.raulrobinson.driven.s3.adapter;

import com.raulrobinson.exception.*;
import com.raulrobinson.model.*;
import com.raulrobinson.ports.out.S3Gateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class S3Adapter implements S3Gateway {

    private static final long TEXT_MAX_BYTES  = 2L  * 1024 * 1024;
    private static final long IMAGE_MAX_BYTES = 10L * 1024 * 1024;

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "image/svg+xml", "image/webp", "image/bmp", "image/x-icon", "image/ico"
    );
    private static final Set<String> TEXT_TYPES = Set.of(
            "text/plain", "text/html", "text/xml", "text/csv", "text/markdown",
            "text/yaml", "text/x-yaml", "text/x-sh", "text/x-python",
            "application/json", "application/xml", "application/yaml",
            "application/x-yaml", "application/javascript", "text/javascript",
            "application/x-sh", "application/x-shellscript", "application/toml"
    );
    private static final Set<String> IMAGE_EXTS = Set.of(
            "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp", "ico"
    );
    private static final Set<String> TEXT_EXTS = Set.of(
            "txt", "log", "csv", "md", "yaml", "yml", "xml", "json", "jsonl",
            "sh", "bash", "py", "js", "ts", "jsx", "tsx", "java", "kt",
            "env", "conf", "ini", "toml", "sql", "html", "htm", "css",
            "properties", "gradle", "tf", "hcl", "graphql", "gql"
    );

    @Override
    public Mono<S3BucketsResult> listBuckets(String accessKeyId, String secretAccessKey,
                                             String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey)
                .then(Mono.defer(() -> {
                    S3AsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return Mono.fromFuture(client.listBuckets())
                            .map(resp -> {
                                List<S3Bucket> buckets = resp.buckets().stream()
                                        .map(b -> new S3Bucket(
                                                b.name(),
                                                b.creationDate() != null ? b.creationDate().toString() : null
                                        ))
                                        .sorted(Comparator.comparing(b -> b.name().toLowerCase()))
                                        .toList();
                                return new S3BucketsResult(buckets);
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<S3ObjectsResult> listObjects(String accessKeyId, String secretAccessKey,
                                             String sessionToken, String region,
                                             String bucket, String prefix, String continuationToken) {
        return validate(accessKeyId, secretAccessKey)
                .then(validateBucket(bucket))
                .then(Mono.defer(() -> {
                    S3AsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    String safePrefix = prefix != null ? prefix : "";

                    ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .delimiter("/")
                            .prefix(safePrefix)
                            .maxKeys(200);

                    if (continuationToken != null && !continuationToken.isBlank()) {
                        builder.continuationToken(continuationToken);
                    }

                    return Mono.fromFuture(client.listObjectsV2(builder.build()))
                            .map(resp -> {
                                List<S3Folder> folders = resp.commonPrefixes().stream()
                                        .map(cp -> new S3Folder(cp.prefix()))
                                        .toList();

                                List<S3ObjectItem> objects = resp.contents().stream()
                                        .filter(obj -> !(obj.key().equals(safePrefix) && obj.size() == 0))
                                        .map(obj -> new S3ObjectItem(
                                                obj.key(), obj.size(),
                                                obj.lastModified() != null ? obj.lastModified().toString() : null,
                                                obj.eTag() != null ? obj.eTag().replace("\"", "") : null,
                                                obj.storageClassAsString()
                                        ))
                                        .toList();

                                return new S3ObjectsResult(
                                        safePrefix, bucket, folders, objects,
                                        resp.isTruncated(), resp.nextContinuationToken(), resp.keyCount()
                                );
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<S3ObjectInfo> getObjectInfo(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region,
                                            String bucket, String key) {
        return validate(accessKeyId, secretAccessKey)
                .then(validateBucketAndKey(bucket, key))
                .then(Mono.defer(() -> {
                    S3AsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return Mono.fromFuture(client.headObject(
                                    HeadObjectRequest.builder().bucket(bucket).key(key).build()))
                            .map(head -> new S3ObjectInfo(
                                    key, bucket,
                                    head.contentLength(),
                                    head.contentType(),
                                    head.lastModified() != null ? head.lastModified().toString() : null,
                                    head.eTag() != null ? head.eTag().replace("\"", "") : null,
                                    head.storageClassAsString(),
                                    head.metadata(),
                                    head.versionId()
                            ))
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<S3ObjectContent> getObjectContent(String accessKeyId, String secretAccessKey,
                                                  String sessionToken, String region,
                                                  String bucket, String key) {
        return validate(accessKeyId, secretAccessKey)
                .then(validateBucketAndKey(bucket, key))
                .then(Mono.defer(() -> {
                    S3AsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);

                    return Mono.fromFuture(client.headObject(
                                    HeadObjectRequest.builder().bucket(bucket).key(key).build()))
                            .flatMap(head -> {
                                long size = head.contentLength() != null ? head.contentLength() : 0L;
                                String contentType = blankFallback(head.contentType(), "application/octet-stream");

                                if (isImage(contentType, key)) {
                                    if (size > IMAGE_MAX_BYTES) {
                                        return Mono.just(new S3ObjectContent(size, contentType, false, "too_large", null, null, null));
                                    }
                                    return Mono.fromFuture(client.getObject(
                                                    GetObjectRequest.builder().bucket(bucket).key(key).build(),
                                                    AsyncResponseTransformer.toBytes()))
                                            .map(bytes -> new S3ObjectContent(
                                                    size, contentType, true, null,
                                                    "image", "base64",
                                                    Base64.getEncoder().encodeToString(bytes.asByteArray())
                                            ));
                                } else if (isText(contentType, key)) {
                                    if (size > TEXT_MAX_BYTES) {
                                        return Mono.just(new S3ObjectContent(size, contentType, false, "too_large", null, null, null));
                                    }
                                    return Mono.fromFuture(client.getObject(
                                                    GetObjectRequest.builder().bucket(bucket).key(key).build(),
                                                    AsyncResponseTransformer.toBytes()))
                                            .map(bytes -> new S3ObjectContent(
                                                    size, contentType, true, null,
                                                    "text", "utf-8",
                                                    bytes.asString(StandardCharsets.UTF_8)
                                            ));
                                } else {
                                    return Mono.just(new S3ObjectContent(size, contentType, false, "unsupported_type", null, null, null));
                                }
                            })
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<S3PresignResult> presignObject(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region,
                                               String bucket, String key) {
        return validate(accessKeyId, secretAccessKey)
                .then(validateBucketAndKey(bucket, key))
                .then(Mono.fromCallable(() -> {
                    var credentials = (sessionToken != null && !sessionToken.isBlank())
                            ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                            : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

                    try (S3Presigner presigner = S3Presigner.builder()
                            .region(Region.of(region))
                            .credentialsProvider(StaticCredentialsProvider.create(credentials))
                            .build()) {

                        String fileName = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;

                        var presigned = presigner.presignGetObject(
                                GetObjectPresignRequest.builder()
                                        .signatureDuration(Duration.ofMinutes(15))
                                        .getObjectRequest(GetObjectRequest.builder()
                                                .bucket(bucket).key(key)
                                                .responseContentDisposition("attachment; filename=\"" + fileName + "\"")
                                                .build())
                                        .build());

                        return new S3PresignResult(presigned.url().toExternalForm());
                    }
                }))
                .onErrorMap(this::mapException);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isImage(String contentType, String key) {
        String base = contentType.split(";")[0].trim().toLowerCase();
        if (IMAGE_TYPES.contains(base) || base.startsWith("image/")) return true;
        return IMAGE_EXTS.contains(getExtension(key));
    }

    private boolean isText(String contentType, String key) {
        String base = contentType.split(";")[0].trim().toLowerCase();
        if (TEXT_TYPES.contains(base) || base.startsWith("text/")) return true;
        return TEXT_EXTS.contains(getExtension(key));
    }

    private String getExtension(String key) {
        int dot = key.lastIndexOf('.');
        return dot >= 0 ? key.substring(dot + 1).toLowerCase() : "";
    }

    private S3AsyncClient buildClient(String region, String accessKeyId,
                                      String secretAccessKey, String sessionToken) {
        var credentials = (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private String blankFallback(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private Mono<Void> validate(String accessKeyId, String secretAccessKey) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta accessKeyId"));
        }
        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta secretAccessKey"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateBucket(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta x-s3-bucket"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateBucketAndKey(String bucket, String key) {
        if (bucket == null || bucket.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta x-s3-bucket"));
        }
        if (key == null || key.isBlank()) {
            return Mono.error(new S3BadRequestException("Falta x-s3-key"));
        }
        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof S3BadRequestException
                || cause instanceof S3ClientException
                || cause instanceof S3AccessDeniedException) {
            return cause;
        }

        if (cause instanceof software.amazon.awssdk.services.s3.model.S3Exception s3e) {
            int status = s3e.statusCode();
            if (status == 403) {
                return new S3AccessDeniedException("Acceso denegado en S3: " + s3e.getMessage(), cause);
            }
            if (status == 404) {
                return new S3ClientException("Recurso S3 no encontrado", cause);
            }
        }

        if (cause instanceof SdkClientException) {
            return new S3ClientException("Error de comunicación con AWS S3", cause);
        }

        return new S3ClientException("Error inesperado consultando AWS S3", cause);
    }

    private Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }
        if (error.getCause() instanceof CompletionException ce && ce.getCause() != null) {
            return ce.getCause();
        }
        return error;
    }
}
