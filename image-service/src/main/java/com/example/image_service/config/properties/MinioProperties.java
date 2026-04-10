package com.example.image_service.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String internalEndpoint;
    private String externalEndpoint;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String region;
    private int presignDuration;
}
