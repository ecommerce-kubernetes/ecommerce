package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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

    @Override
    public CategoryResponseDto getCategoryDetails(Long categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        return new CategoryResponseDto(category);
    }

    @Override
    public PageDto<CategoryResponseDto> getCategoryList(Pageable pageable) {
        Page<Categories> result = categoriesRepository.findAllCategories(pageable);
        List<Categories> content = result.getContent();
        List<CategoryResponseDto> categoryResponseList = content.stream().map(CategoryResponseDto::new).toList();
        return new PageDto<>(
                categoryResponseList,
                pageable.getPageNumber(),
                result.getTotalPages(),
                pageable.getPageSize(),
                result.getTotalElements()
        );
    }
}
