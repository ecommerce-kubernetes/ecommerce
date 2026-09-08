package com.example.product_service.option.domain.context;

import java.util.List;

public record CreateOptionTypeContext(
        Long id,
        String name,
        List<CreateOptionValueContext> valueContexts
){
}
