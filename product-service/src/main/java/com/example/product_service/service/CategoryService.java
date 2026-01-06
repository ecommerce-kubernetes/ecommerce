package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.controller.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Category;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.product_service.common.MessagePath.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MessageSourceUtil ms;

    public CategoryResponse updateCategory(Long categoryId, String name, String imageUrl) {
        return null;
    }

    public CategoryResponse moveParent(Long categoryId, Long parentId) {
        return null;
    }

    @Transactional
    public CategoryResponse saveCategory(CategoryRequest request) {
        checkConflictName(request.getName());
        Category category = new Category(request.getName(), request.getImageUrl());
        if(request.getParentId() != null){
            Category parent = findByIdOrThrow(request.getParentId());
            parent.addChild(category);
        }
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved);
    }

    public List<CategoryResponse> getRootCategories() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        return roots.stream().map(CategoryResponse::new).toList();
    }

    public List<CategoryResponse> getChildrenCategoriesById(Long categoryId) {
        Category target = findByIdOrThrow(categoryId);
        return target.getChildren().stream().map(CategoryResponse::new).toList();
    }

    public CategoryHierarchyResponse getHierarchyByCategoryId(Long categoryId) {
        Category target = findWithParentByIdOrThrow(categoryId);
        CategoryHierarchyResponse response = new CategoryHierarchyResponse();

        List<Category> ancestorChain = buildAncestorPath(target);
        setAncestors(response, ancestorChain);

        setSiblingsByLevel(1, response, categoryRepository.findByParentIsNull());

        for(int i=0; i<ancestorChain.size(); i++){
            Category category = ancestorChain.get(i);
            if(!category.getChildren().isEmpty()){
                setSiblingsByLevel(i+2, response, category.getChildren());
            }
        }
        return response;
    }

    @Transactional
    public CategoryResponse updateCategoryById(Long categoryId, UpdateCategoryRequest request) {
        Category target = findByIdOrThrow(categoryId);
        if (request.getName() != null){
            checkConflictName(request.getName());
            target.setName(request.getName());
        }
        if (request.getIconUrl() != null){
            target.setIconUrl(request.getIconUrl());
        }
        if (request.getParentId() != null){
            checkParentIsNotSelf(target.getId(), request.getParentId());
            Category parent = findByIdOrThrow(request.getParentId());
            target.modifyParent(parent);
        }
        return new CategoryResponse(target);
    }

    @Transactional
    public void deleteCategoryById(Long categoryId) {
        Category target = findByIdOrThrow(categoryId);
        categoryRepository.delete(target);
    }

    private void checkConflictName(String name) {
        if(categoryRepository.existsByName(name)){
            throw new DuplicateResourceException(ms.getMessage(CATEGORY_CONFLICT));
        }
    }

    private Category findByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }

    private Category findWithParentByIdOrThrow(Long categoryId){
        return categoryRepository.findWithParentById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }

    private void checkParentIsNotSelf(Long targetId, Long parentId){
        if(Objects.equals(targetId, parentId)){
            throw new BadRequestException(ms.getMessage(CATEGORY_BAD_REQUEST));
        }
    }

    private List<Category> buildAncestorPath(Category category) {
        if (category == null) {
            return new ArrayList<>();
        }
        List<Category> ancestors = buildAncestorPath(category.getParent());
        ancestors.add(category);
        return ancestors;
    }

    private void setSiblingsByLevel(int level, CategoryHierarchyResponse response, List<Category> categories){
        response.getSiblingsByLevel().add(new CategoryHierarchyResponse.LevelItem(level, categories.stream().map(CategoryResponse::new).toList()));
    }

    private void setAncestors(CategoryHierarchyResponse response, List<Category> ancestors){
        response.setAncestors(ancestors.stream()
                .map(CategoryResponse::new).toList());
    }
}
