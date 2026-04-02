package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CategoryCreateRequest(
        @NotBlank(message = "name은 필수값입니다")
        String name,
        Long parentId,
        @Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
        )
        String imagePath
) {
}
