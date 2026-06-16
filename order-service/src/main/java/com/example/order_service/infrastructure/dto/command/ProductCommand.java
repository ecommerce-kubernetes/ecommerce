package com.example.order_service.infrastructure.dto.command;

import lombok.Builder;

import java.util.List;

public class ProductCommand {

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
