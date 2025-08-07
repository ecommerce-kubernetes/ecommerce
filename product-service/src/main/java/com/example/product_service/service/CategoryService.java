package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Categories;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CategoryService {
    CategoryResponse saveCategory(CategoryRequest categoryRequestDto);
    CategoryResponse modifyCategory(Long categoryId, UpdateCategoryRequest requestDto);
    void deleteCategory(Long categoryId);
    CategoryResponse getCategoryDetails(Long categoryId);
    PageDto<CategoryResponse> getRootCategories(Pageable pageable);
    List<CategoryResponse> getChildCategories(Long categoryId);
    CategoryResponse getRootCategoryDetailsOf(Long categoryId);
    Categories getByIdOrThrow(Long categoryId);
    List<Long> getCategoryAndDescendantIds(Long categoryId);
}
