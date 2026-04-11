package com.example.image_service.service;

import com.example.image_service.config.properties.MinioProperties;
import com.example.image_service.service.dto.result.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final S3Presigner s3Presigner;
    private final MinioProperties properties;

    public PresignedUrlResponse generatePresignedUrl(String domain, String originalFilename) {
        String objectKey = createObjectKey(domain, originalFilename);
        String extension = extractExtension(originalFilename);
        String contentType = determineContentType(extension);
        String presignedUrl = issuedPresignedUrl(objectKey, properties.getBucket(), properties.getPresignDuration(), contentType);
        return createPresignedUrlResponse(presignedUrl, objectKey);
    }

    private PresignedUrlResponse createPresignedUrlResponse(String presignedUrl, String objectKey) {
        return PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .imageUrl(objectKey)
                .build();
    }

    private String issuedPresignedUrl(String objectKey, String bucket, int duration, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(duration))
                .putObjectRequest(objectRequest)
                .build();
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    private String createObjectKey(String domain, String filename) {
        String extension = extractExtension(filename);
        return domain + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private String determineContentType(String extension) {
        if (extension == null) return "application/octet-stream";

        return switch (extension.toLowerCase()) {
            case ".png" -> "image/png";
            case ".jpg", "jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
