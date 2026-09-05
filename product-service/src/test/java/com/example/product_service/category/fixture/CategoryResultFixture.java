package com.example.product_service.category.fixture;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;

import java.util.List;

public class CategoryResultFixture {

    public static RootCategoriesResult.RootCategoriesResultBuilder anRootCategoriesResult() {
        CategoryResult food = anCategoryResult().build();
        CategoryResult furniture = anCategoryResult()
                .id(2L)
                .name("가구")
                .path("2")
                .imagePath("/categories/furniture.jpg")
                .build();

        return RootCategoriesResult.builder()
                .categories(List.of(food, furniture));
    }

    public static CategoryResult.CategoryResultBuilder anCategoryResult() {
        return CategoryResult.builder()
                .id(1L)
                .name("식품")
                .depth(1)
                .path("1")
                .imagePath("/categories/food.jpg")
                .isLeaf(false);
    }
}
