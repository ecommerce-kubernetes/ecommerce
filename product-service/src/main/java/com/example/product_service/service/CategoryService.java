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
        Categories target = findByIdWithParentOrThrow(categoryId);

        List<Categories> chain = new ArrayList<>();
        while(target.getParent() != null){
            chain.add(target);
            target = target.getParent();
        }
        chain.add(target);
        Collections.reverse(chain);
        CategoryHierarchyResponse response = new CategoryHierarchyResponse();

        response.getSiblingsByLevel().add(new CategoryHierarchyResponse.LevelItem(
                1, categoryRepository.findByParentIsNull().stream().map(CategoryResponse::new).toList()
        ));
        for(int i=0; i<chain.size(); i++){
            Categories category = chain.get(i);
            response.getAncestors().add(new CategoryResponse(category));
            response.getSiblingsByLevel().add(
                    new CategoryHierarchyResponse.LevelItem(
                            i+2,
                            category.getChildren().stream().map(CategoryResponse::new).toList()
                    )
            );
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

    public CategoryResponse getCategoryDetails(Long categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        return new CategoryResponse(category);
    }

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

    private Categories findByIdWithParentOrThrow(Long categoryId){
        return categoryRepository.findWithParentById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage("category.notFound")));
    }

    private void checkParentIsNotSelf(Long targetId, Long parentId){
        if(Objects.equals(targetId, parentId)){
            throw new BadRequestException(ms.getMessage("category.badRequest"));
        }
    }
}
