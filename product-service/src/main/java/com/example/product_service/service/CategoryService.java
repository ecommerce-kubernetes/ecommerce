package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Categories;

import java.util.List;


public interface CategoryService {
    CategoryResponse saveCategory(CategoryRequest categoryRequestDto);
    CategoryResponse updateCategoryById(Long categoryId, UpdateCategoryRequest requestDto);
    void deleteCategoryById(Long categoryId);
    CategoryResponse getCategoryDetails(Long categoryId);
    List<CategoryResponse> getRootCategories();
    CategoryHierarchyResponse getHierarchyByCategoryId(Long categoryId);
    List<CategoryResponse> getChildrenCategoriesById(Long categoryId);
    CategoryResponse getRootCategoryDetailsOf(Long categoryId);
    Categories getByIdOrThrow(Long categoryId);
    List<Long> getCategoryAndDescendantIds(Long categoryId);
}
