package com.example.product_service.product.domain.context;

import lombok.Builder;

@Builder
public record AddMainImageContext(Long id, String imagePath) {
}
