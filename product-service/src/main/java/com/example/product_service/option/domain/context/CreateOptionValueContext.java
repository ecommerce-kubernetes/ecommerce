package com.example.product_service.option.domain.context;

import lombok.Builder;

@Builder
public record CreateOptionValueContext(
        Long id,
        String name
) {

    public static CreateOptionValueContext of(Long id, String name) {
        return CreateOptionValueContext.builder()
                .id(id)
                .name(name)
                .build();
    }
}
