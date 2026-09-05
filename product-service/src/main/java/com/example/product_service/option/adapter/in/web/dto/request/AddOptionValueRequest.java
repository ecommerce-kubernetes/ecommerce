package com.example.product_service.option.adapter.in.web.dto.request;

import lombok.Builder;

@Builder
public record AddOptionValueRequest(
        String name
) {
}
