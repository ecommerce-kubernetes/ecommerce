package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponse;
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
    public CategoryResponse saveCategory(CategoryRequest categoryRequestDto) {
        Categories category = new Categories(categoryRequestDto.getName(), categoryRequestDto.getIconUrl());
        Long parentId = categoryRequestDto.getParentId();

        // parentId null 이 아닐시 부모카테고리 child 에 추가
        if(parentId != null){
            Categories parentCategory = categoriesRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Not Found Parent Category"));

            parentCategory.addChild(category);
        }

        Categories save = categoriesRepository.save(category);
        return new CategoryResponse(save);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategoryById(Long categoryId, UpdateCategoryRequest requestDto) {
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

        return new CategoryResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        if(category.getParent() != null){
            category.getParent().removeChild(category);
        }
        categoriesRepository.delete(category);
    }

    @Override
    public CategoryResponse getCategoryDetails(Long categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        return new CategoryResponse(category);
    }

    @Override
    public CategoryHierarchyResponse getHierarchyByCategoryId(Long categoryId) {
        return null;
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        return null;
    }

    @Override
    public List<CategoryResponse> getChildrenCategoriesById(Long categoryId) {
        List<Categories> childList = categoriesRepository.findChildById(categoryId);
        return childList.stream().map(CategoryResponse::new).toList();
    }

    @Override
    public CategoryResponse getRootCategoryDetailsOf(Long categoryId) {
        Categories category = categoriesRepository.findByIdWithParent(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        while (category.getParent() != null) {
            category = category.getParent();
        }

        return new CategoryResponse(category);
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
