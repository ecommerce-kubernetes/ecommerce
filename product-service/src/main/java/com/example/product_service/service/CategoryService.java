package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import org.springframework.data.domain.Pageable;


public interface CategoryService {
    CategoryResponseDto saveCategory(CategoryRequestDto categoryRequestDto);
    CategoryResponseDto modifyCategory(Long categoryId, CategoryRequestDto categoryRequestDto);
    void deleteCategory(Long categoryId);
    CategoryResponseDto getCategoryDetails(Long categoryId);
    PageDto<CategoryResponseDto> getCategoryList(Pageable pageable);
}
