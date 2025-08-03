package com.example.product_service.dto.response.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CategoryHierarchyResponse {
    private List<CategoryResponseDto> ancestors;
    private List<LevelItem> siblingsByLevel;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LevelItem {
        private int level;
        private List<CategoryResponseDto> items;
    }
}
