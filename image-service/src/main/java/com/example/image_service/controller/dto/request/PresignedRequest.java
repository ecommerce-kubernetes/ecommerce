package com.example.image_service.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedRequest {
    @NotBlank(message = "domain은 필수값 입니다")
    private String domain;
    @NotBlank(message = "originalFilename 은 필수값 입니다")
    private String originalFilename;
}
