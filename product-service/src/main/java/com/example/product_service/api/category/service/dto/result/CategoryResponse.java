package com.example.product_service.api.category.service.dto.result;

import com.example.product_service.api.category.domain.model.Category;
import lombok.*;

@Getter
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer depth;
    private String imageUrl;

    public CategoryResponse(Category category){
        this.id = category.getId();
        this.name = category.getName();
        this.parentId = category.getParent() == null ? null : category.getParent().getId();
        this.imageUrl = category.getImageUrl();
    }

    @Builder
    public CategoryResponse(Long id, String name, Long parentId, Integer depth, String imageUrl) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.depth = depth;
        this.imageUrl = imageUrl;
    }

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() == null ? null : category.getParent().getId())
                .depth(category.getDepth())
                .imageUrl(category.getImageUrl())
                .build();
    }
}
