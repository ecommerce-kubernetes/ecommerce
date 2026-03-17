package com.example.image_service.config;

import com.example.image_service.config.properties.MinioProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "minio", name = "initialize", havingValue = "true", matchIfMissing = true)
public class MinioBucketInitializer {
    private final S3Client s3Client;
    private final MinioProperties properties;

    @PostConstruct
    public void initializeBucket() {
        try {
            createBucket();
            setPolicy();
        } catch (S3Exception e) {
            log.error("MinIO 버킷 초기화 실패");
        }
    }

    private void createBucket() {
        List<Bucket> buckets = s3Client.listBuckets().buckets();
        boolean bucketExists = buckets.stream()
                .anyMatch(b -> b.name().equals(properties.getBucket()));

        if (!bucketExists) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(properties.getBucket())
                    .build());
        }
    }

    private void setPolicy() {
        String publicPolicy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """.formatted(properties.getBucket());

        s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                .bucket(properties.getBucket())
                .policy(publicPolicy)
                .build());
    }
}
