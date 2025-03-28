package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService{

    private final CategoriesRepository categoriesRepository;

    @Override
    @Transactional
    public CategoryResponseDto saveCategory(CategoryRequestDto categoryRequestDto) {
        Categories categories = new Categories(categoryRequestDto.getName());
        Categories save = categoriesRepository.save(categories);

        return new CategoryResponseDto(save);
    }

    @Override
    @Transactional
    public CategoryResponseDto modifyCategory(Long categoryId, CategoryRequestDto categoryRequestDto) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        category.setName(categoryRequestDto.getName());

        return new CategoryResponseDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        categoriesRepository.delete(category);
    }
}
