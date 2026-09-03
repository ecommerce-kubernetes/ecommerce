package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.ProductCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductImageRequest(
        @NotEmpty(message = "최소 1장의 이미지를 등록해야 합니다")
        List<@Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다") String> images
) {
    public ProductCommand.AddImage toCommand(Long productId) {
        return ProductCommand.AddImage.builder()
                .productId(productId)
                .images(images)
                .build();
    }
}
