package com.example.product_service.product.application.service.dto.command;

import lombok.Builder;
import org.springframework.data.domain.Pageable;

@Builder
public record SearchProductCommand(
        Long categoryId,
        String name,
        Integer rating,
        String sort,
        Pageable pageable
) {
    public static SearchProductCommand of(Long categoryId, String name, Integer rating, String sort, Pageable pageable) {
        return SearchProductCommand.builder()
                .categoryId(categoryId)
                .name(name)
                .rating(rating)
                .sort(sort)
                .pageable(pageable)
                .build();
    }
}
