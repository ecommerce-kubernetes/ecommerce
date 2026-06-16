package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

import java.util.List;

public class ProductClientRequest {

    @Builder
    public record BulkSearch(
            List<Long> variantIds
    ) {

        public static BulkSearch from(List<Long> variantIds) {
            return BulkSearch.builder()
                    .variantIds(variantIds)
                    .build();
        }
    }
}
