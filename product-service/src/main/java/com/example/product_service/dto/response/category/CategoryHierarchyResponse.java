package com.example.product_service.dto.response.category;

import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHierarchyResponse {
    private List<CategoryResponse> ancestors = new ArrayList<>();
    private List<LevelItem> siblingsByLevel = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelItem {
        private int level;
        private List<CategoryResponse> items;
    }
}
