package com.example.image_service.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlResponse {
    private String presignedUrl;
    private String imageUrl;
}
