package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MessageSourceUtil ms;
    
    @Transactional
    public CategoryResponse saveCategory(CategoryRequest request) {
        checkConflictName(request.getName());
        Categories categories = new Categories(request.getName(), request.getIconUrl());
        if(request.getParentId() != null){
            Categories parent = findByIdOrThrow(request.getParentId());
            parent.addChild(categories);
        }
        Categories saved = categoryRepository.save(categories);
        return new CategoryResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategoryById(Long categoryId, UpdateCategoryRequest request) {
        Categories target = findByIdOrThrow(categoryId);

        if (request.getName() != null){
            checkConflictName(request.getName());
            target.setName(request.getName());
        }
        if (request.getIconUrl() != null){
            target.setIconUrl(request.getIconUrl());
        }
        if (request.getParentId() != null){
            checkMySelfForParent(categoryId, request.getParentId());
            Categories parent = findByIdOrThrow(request.getParentId());
            target.modifyParent(parent);
        }
        return new CategoryResponse(target);
    }

    @Transactional
    public void deleteCategoryById(Long categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        if(category.getParent() != null){
            category.getParent().removeChild(category);
        }
        categoryRepository.delete(category);
    }

    public CategoryResponse getCategoryDetails(Long categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        return new CategoryResponse(category);
    }

    public CategoryHierarchyResponse getHierarchyByCategoryId(Long categoryId) {
        return null;
    }

    public List<CategoryResponse> getRootCategories() {
        return null;
    }


    public List<CategoryResponse> getChildrenCategoriesById(Long categoryId) {
        List<Categories> childList = categoryRepository.findChildById(categoryId);
        return childList.stream().map(CategoryResponse::new).toList();
    }

    public CategoryResponse getRootCategoryDetailsOf(Long categoryId) {
        Categories category = categoryRepository.findByIdWithParent(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        while (category.getParent() != null) {
            category = category.getParent();
        }

        return new CategoryResponse(category);
    }

    private void checkConflictName(String name) {
        if(categoryRepository.existsByName(name)){
            throw new DuplicateResourceException(ms.getMessage("category.conflict"));
        }
    }

    private Categories findByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage("category.notFound")));
    }

    private void checkMySelfForParent(Long targetId, Long parentId){
        if(Objects.equals(targetId, parentId)){
            throw new BadRequestException("category.badRequest");
        }
    }
}
