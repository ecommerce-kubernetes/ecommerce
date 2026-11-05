package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.AddProductDescriptionImageCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductDescriptionImageRequest(
        @NotEmpty(message = "{product.images.notEmpty}")
        List<@Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "{product.images.pattern}") String> images
) {
    public AddProductDescriptionImageCommand toCommand(Long productId) {
        return AddProductDescriptionImageCommand.builder()
                .productId(productId)
                .images(images)
                .build();
    }
}
