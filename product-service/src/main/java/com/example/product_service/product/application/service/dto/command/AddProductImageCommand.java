package com.example.product_service.product.application.service.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record AddProductImageCommand(
        Long productId,
        List<String> images
) {
}
