package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotBlank(message = "{NotBlank}")
    private String name;
    private Long parentId;
    @URL(message = "{InvalidUrl}")
    private String imageUrl;

    @Builder
    public CategoryRequest(String name, Long parentId, String imageUrl) {
        this.name = name;
        this.parentId = parentId;
        this.imageUrl = imageUrl;
    }
}
