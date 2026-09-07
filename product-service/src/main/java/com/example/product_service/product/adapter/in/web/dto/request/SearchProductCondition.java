package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.SearchProductCommand;
import org.springframework.data.domain.Pageable;

public record SearchProductCondition(
        Long categoryId,
        String name,
        Integer rating,
        String sort
) {
    public SearchProductCommand toCommand(Pageable pageable) {
        return SearchProductCommand.builder()
                .categoryId(categoryId)
                .name(name)
                .rating(rating)
                .sort(sort != null ? sort : "latest")
                .pageable(pageable)
                .build();
    }
}
