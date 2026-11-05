package com.example.product_service.category.adapter.in.web.dto.request;

import lombok.Builder;

@Builder
public record MoveCategoryRequest(
        Long newParentId
) { }
