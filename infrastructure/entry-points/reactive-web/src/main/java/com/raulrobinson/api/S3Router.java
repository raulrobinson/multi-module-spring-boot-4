package com.raulrobinson.api;

import com.raulrobinson.api.handlers.S3Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class S3Router {

    @Value("${app.base-path}")
    private String basePath;

    @Value("${app.version}")
    private String version;

    private final S3Handler handler;

    @Bean
    public RouterFunction<ServerResponse> s3Routes() {
        return RouterFunctions.route()
                .POST(basePath + "/" + version + "/s3/buckets",     handler::listBuckets)
                .POST(basePath + "/" + version + "/s3/objects",     handler::listObjects)
                .POST(basePath + "/" + version + "/s3/object-info", handler::objectInfo)
                .POST(basePath + "/" + version + "/s3/content",     handler::getObjectContent)
                .POST(basePath + "/" + version + "/s3/presign",     handler::presignObject)
                .build();
    }
}
