package com.example.product_service.dto.response.category;

import com.example.product_service.entity.Category;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String iconUrl;

    public CategoryResponse(Category category){
        this.id = category.getId();
        this.name = category.getName();
        this.parentId = category.getParent() == null ? null : category.getParent().getId();
        this.iconUrl = category.getIconUrl();
    }
}
