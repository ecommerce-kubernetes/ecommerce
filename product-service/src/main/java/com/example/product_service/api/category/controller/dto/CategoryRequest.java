package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotBlank(message = "name은 필수값입니다")
    private String name;
    private Long parentId;
    @URL(message = "imageUrl 형식은 URL 형식이여야합니다")
    private String imageUrl;

    @Builder
    public CategoryRequest(String name, Long parentId, String imageUrl) {
        this.name = name;
        this.parentId = parentId;
        this.imageUrl = imageUrl;
    }
}
