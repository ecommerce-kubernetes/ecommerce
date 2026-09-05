package com.example.product_service.category.fixture;

import com.example.product_service.category.application.service.dto.result.*;

import java.util.Collections;
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

    public static ChildCategoriesResult.ChildCategoriesResultBuilder anChildCategoriesResult() {
        CategoryResult meat = anCategoryResult()
                .id(3L)
                .name("육류")
                .depth(2)
                .path("1/3")
                .imagePath("/categories/meat.jpg")
                .isLeaf(true)
                .build();

        CategoryResult vegetable = anCategoryResult()
                .id(4L)
                .name("채소")
                .depth(2)
                .path("1/4")
                .imagePath("/categories/vegetable.jpg")
                .isLeaf(true)
                .build();

        return ChildCategoriesResult.builder()
                .categories(List.of(meat, vegetable));
    }

    public static DetailCategoryResult.DetailCategoryResultBuilder anDetailCategoryResult() {
        CategoryResult food = anCategoryResult().build();
        CategoryResult meat = anCategoryResult()
                .id(3L)
                .name("육류")
                .depth(2)
                .path("1/3")
                .imagePath("/categories/meat.jpg")
                .isLeaf(true)
                .build();
        return DetailCategoryResult.builder()
                .id(3L)
                .name("육류")
                .depth(2)
                .path("1/3")
                .imagePath("/categories/meat.jpg")
                .isLeaf(true)
                .breadcrumb(List.of(food, meat));
    }

    public static TreeCategoriesResult.TreeCategoriesResultBuilder anTreeCategoriesResult() {
        TreeCategoriesResult.TreeCategoryResult meat = TreeCategoriesResult.TreeCategoryResult.builder()
                .id(3L)
                .name("육류")
                .depth(2)
                .path("1/3")
                .imagePath("/categories/meat.jpg")
                .isLeaf(true)
                .children(Collections.emptyList())
                .build();

        TreeCategoriesResult.TreeCategoryResult vegetable = TreeCategoriesResult.TreeCategoryResult.builder()
                .id(4L)
                .name("채소")
                .depth(2)
                .path("1/4")
                .imagePath("/categories/vegetable.jpg")
                .isLeaf(true)
                .children(Collections.emptyList())
                .build();

        TreeCategoriesResult.TreeCategoryResult food = TreeCategoriesResult.TreeCategoryResult.builder()
                .id(1L)
                .name("식품")
                .depth(1)
                .path("1")
                .imagePath("/categories/food.jpg")
                .isLeaf(false)
                .children(List.of(meat, vegetable))
                .build();

        TreeCategoriesResult.TreeCategoryResult chair = TreeCategoriesResult.TreeCategoryResult.builder()
                .id(5L)
                .name("의자")
                .depth(2)
                .path("2/5")
                .imagePath("/categories/chair.jpg")
                .isLeaf(true)
                .children(Collections.emptyList())
                .build();

        TreeCategoriesResult.TreeCategoryResult desk = TreeCategoriesResult.TreeCategoryResult.builder()
                .id(6L)
                .name("책상")
                .depth(2)
                .path("2/6")
                .imagePath("/categories/desk.jpg")
                .isLeaf(true)
                .children(Collections.emptyList())
                .build();

        TreeCategoriesResult.TreeCategoryResult furniture = TreeCategoriesResult.TreeCategoryResult.builder()
                .id(2L)
                .name("가구")
                .depth(1)
                .path("2")
                .imagePath("/categories/furniture.jpg")
                .isLeaf(false)
                .children(List.of(desk, chair))
                .build();

        return TreeCategoriesResult.builder()
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
