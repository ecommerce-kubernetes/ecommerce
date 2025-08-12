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

import java.util.*;


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

    public List<CategoryResponse> getRootCategories() {
        List<Categories> roots = categoryRepository.findByParentIsNull();
        return roots.stream().map(CategoryResponse::new).toList();
    }

    public List<CategoryResponse> getChildrenCategoriesById(Long categoryId) {
        Categories target = findByIdOrThrow(categoryId);
        return target.getChildren().stream().map(CategoryResponse::new).toList();
    }

    public CategoryHierarchyResponse getHierarchyByCategoryId(Long categoryId) {
        Categories target = findWithParentByIdOrThrow(categoryId);
        CategoryHierarchyResponse response = new CategoryHierarchyResponse();

        List<Categories> ancestorChain = buildAncestorPath(target);
        setAncestors(response, ancestorChain);

        setSiblingsByLevel(1, response, categoryRepository.findByParentIsNull());

        for(int i=0; i<ancestorChain.size(); i++){
            Categories category = ancestorChain.get(i);
            if(!category.getChildren().isEmpty()){
                setSiblingsByLevel(i+2, response, category.getChildren());
            }
        }
        return response;
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
            checkParentIsNotSelf(target.getId(), request.getParentId());
            Categories parent = findByIdOrThrow(request.getParentId());
            target.modifyParent(parent);
        }
        return new CategoryResponse(target);
    }

    @Transactional
    public void deleteCategoryById(Long categoryId) {
        Categories target = findByIdOrThrow(categoryId);
        categoryRepository.delete(target);
    }

    //TODO 삭제 예정
    public CategoryResponse getCategoryDetails(Long categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));
        return new CategoryResponse(category);
    }

    //TODO 삭제 예정
    public CategoryResponse getRootCategoryDetailsOf(Long categoryId) {
        Categories category = categoryRepository.findWithParentById(categoryId)
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

    private Categories findWithParentByIdOrThrow(Long categoryId){
        return categoryRepository.findWithParentById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage("category.notFound")));
    }

    private void checkParentIsNotSelf(Long targetId, Long parentId){
        if(Objects.equals(targetId, parentId)){
            throw new BadRequestException(ms.getMessage("category.badRequest"));
        }
    }

    private List<Categories> buildAncestorPath(Categories category) {
        if (category == null) {
            return new ArrayList<>();
        }
        List<Categories> ancestors = buildAncestorPath(category.getParent());
        ancestors.add(category);
        return ancestors;
    }

    private void setSiblingsByLevel(int level, CategoryHierarchyResponse response, List<Categories> categories){
        response.getSiblingsByLevel().add(new CategoryHierarchyResponse.LevelItem(level, categories.stream().map(CategoryResponse::new).toList()));
    }

    private void setAncestors(CategoryHierarchyResponse response, List<Categories> ancestors){
        response.setAncestors(ancestors.stream()
                .map(CategoryResponse::new).toList());
    }
}
