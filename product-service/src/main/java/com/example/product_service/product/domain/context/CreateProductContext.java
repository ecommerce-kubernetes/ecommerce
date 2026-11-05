package com.example.product_service.product.domain.context;

import lombok.Builder;

@Builder
public record CreateProductContext(Long id, Long categoryId, String name, String description) {
}
