package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductBulkSearchRequest(
        List<Long> productVariantId
) {
}
