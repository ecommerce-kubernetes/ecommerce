package com.example.product_service.product.domain.context;

import lombok.Builder;

@Builder
public record AddDetailImageContext(Long id, String imagePath) {
}
