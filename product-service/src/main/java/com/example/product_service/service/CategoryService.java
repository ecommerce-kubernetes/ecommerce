package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Categories;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CategoryService {
    CategoryResponseDto saveCategory(CategoryRequest categoryRequestDto);
    CategoryResponseDto modifyCategory(Long categoryId, UpdateCategoryRequest requestDto);
    void deleteCategory(Long categoryId);
    CategoryResponseDto getCategoryDetails(Long categoryId);
    PageDto<CategoryResponseDto> getRootCategories(Pageable pageable);
    List<CategoryResponseDto> getChildCategories(Long categoryId);
    CategoryResponseDto getRootCategoryDetailsOf(Long categoryId);
    Categories getByIdOrThrow(Long categoryId);
    List<Long> getCategoryAndDescendantIds(Long categoryId);
}
