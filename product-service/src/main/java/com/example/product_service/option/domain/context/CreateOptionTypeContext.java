package com.example.product_service.option.domain.context;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateOptionTypeContext(
        Long id,
        String name,
        List<CreateOptionValueContext> valueContexts
){
    public static CreateOptionTypeContext of(Long id, String name, List<CreateOptionValueContext> valueContexts) {
        return CreateOptionTypeContext.builder()
                .id(id)
                .name(name)
                .valueContexts(valueContexts)
                .build();
    }
}
