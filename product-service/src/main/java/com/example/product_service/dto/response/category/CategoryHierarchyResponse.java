package com.example.product_service.dto.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHierarchyResponse {
    private List<CategoryResponse> ancestors;
    private List<LevelItem> siblingsByLevel;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelItem {
        private int level;
        private List<CategoryResponse> items;
    }
}
