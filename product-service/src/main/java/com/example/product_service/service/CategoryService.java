package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;

public interface CategoryService {
    CategoryResponseDto saveCategory(CategoryRequestDto categoryRequestDto);
    CategoryResponseDto modifyCategory(Long categoryId, CategoryRequestDto categoryRequestDto);
    void deleteCategory(Long categoryId);
}
