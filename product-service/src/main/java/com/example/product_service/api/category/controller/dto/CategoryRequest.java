package com.example.product_service.api.category.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "{NotBlank}")
    private String name;
    private Long parentId;
    @URL(message = "{InvalidUrl}")
    private String iconUrl;

    @Builder
    public CategoryRequest(String name, Long parentId, String iconUrl) {
        this.name = name;
        this.parentId = parentId;
        this.iconUrl = iconUrl;
    }
}
