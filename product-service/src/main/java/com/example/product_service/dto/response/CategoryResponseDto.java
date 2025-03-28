package com.example.product_service.dto.response;

import com.example.product_service.entity.Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String name;

    public CategoryResponseDto(Categories category){
        this.id = category.getId();
        this.name = category.getName();
    }
}
