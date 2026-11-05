package com.example.product_service.product.domain.context;

import lombok.Builder;

@Builder
public record RegisterOptionTypeContext(Long id, Long optionTypeId) {
}
