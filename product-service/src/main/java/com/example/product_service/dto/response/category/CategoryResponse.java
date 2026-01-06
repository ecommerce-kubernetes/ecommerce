package com.example.product_service.dto.response.category;

import com.example.product_service.entity.Category;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String imageUrl;

    public CategoryResponse(Category category){
        this.id = category.getId();
        this.name = category.getName();
        this.parentId = category.getParent() == null ? null : category.getParent().getId();
        this.imageUrl = category.getIconUrl();
    }

    @Builder
    public CategoryResponse(Long id, String name, Long parentId, String imageUrl) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.imageUrl = imageUrl;
    }
}
