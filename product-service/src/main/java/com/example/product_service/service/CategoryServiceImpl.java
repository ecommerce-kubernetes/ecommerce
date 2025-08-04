package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.request.ModifyCategoryRequestDto;
import com.example.product_service.dto.response.category.CategoryResponseDto;
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
    public CategoryResponseDto saveCategory(CategoryRequest categoryRequestDto) {
        Categories category = new Categories(categoryRequestDto.getName(), categoryRequestDto.getIconUrl());
        Long parentId = categoryRequestDto.getParentId();

        // parentId null 이 아닐시 부모카테고리 child 에 추가
        if(parentId != null){
            Categories parentCategory = categoriesRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Not Found Parent Category"));

            parentCategory.addChild(category);
        }

        Categories save = categoriesRepository.save(category);
        return new CategoryResponseDto(save);
    }

    @Override
    @Transactional
    public CategoryResponseDto modifyCategory(Long categoryId, ModifyCategoryRequestDto requestDto) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));


        // name 이 null 이 아닌 경우 변경
        if(requestDto.getName() != null){
            category.setName(requestDto.getName());
        }

        // iconUrl 이 null 이 아닌경우 변경
        if(requestDto.getIconUrl() != null){
            category.setIconUrl(requestDto.getIconUrl());
        }

        Long parentId = requestDto.getParentId();
        // parentId null 이 아닐시 부모카테고리 변경
        if(parentId != null){
            Categories newParent = categoriesRepository.findById(parentId).orElseThrow(
                            () -> new NotFoundException("Not Found Parent Category"));
            category.modifyParent(newParent);
        }

        return new CategoryResponseDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        if(category.getParent() != null){
            category.getParent().removeChild(category);
        }
        categoriesRepository.delete(category);
    }

    @Override
    public CategoryResponseDto getCategoryDetails(Long categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        return new CategoryResponseDto(category);
    }

    @Override
    public PageDto<CategoryResponseDto> getRootCategories(Pageable pageable) {
        Page<Categories> result = categoriesRepository.findByParentIsNull(pageable);
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

    @Override
    public List<CategoryResponseDto> getChildCategories(Long categoryId) {
        List<Categories> childList = categoriesRepository.findChildById(categoryId);
        return childList.stream().map(CategoryResponseDto::new).toList();
    }

    @Override
    public CategoryResponseDto getRootCategoryDetailsOf(Long categoryId) {
        Categories category = categoriesRepository.findByIdWithParent(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        while (category.getParent() != null) {
            category = category.getParent();
        }

        return new CategoryResponseDto(category);
    }

    @Override
    public Categories getByIdOrThrow(Long categoryId) {
        return categoriesRepository.findById(categoryId)
                .orElseThrow(()-> new NotFoundException("Not Found Category"));
    }

    @Override
    public List<Long> getCategoryAndDescendantIds(Long categoryId) {
        return categoriesRepository.findAllCategoryTreeIds(categoryId);
    }

}
