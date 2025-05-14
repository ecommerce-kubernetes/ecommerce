package com.example.product_service.dto.response;

import com.example.product_service.entity.Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String name;
    private List<CategoryResponseDto> children = new ArrayList<>();

    public CategoryResponseDto(Categories category){
        this.id = category.getId();
        this.name = category.getName();
        for (Categories child : category.getChildren()){
            this.children.add(new CategoryResponseDto(child));
        }
    }
}
