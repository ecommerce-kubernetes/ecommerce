package com.example.product_service.api.category.service;

import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public CategoryResponse saveCategory(String name, Long parentId, String imageUrl) {
        Category parent = findParentByCategory(parentId);
        if (productRepository.existsByCategoryId(parentId)){
            throw new BusinessException(CategoryErrorCode.HAS_PRODUCT);
        }
        Category category = Category.create(name, parent, imageUrl);
        categoryRepository.save(category);
        category.generatePath();
        return CategoryResponse.from(category);
    }

    public CategoryResponse getCategory(Long categoryId) {
        return null;
    }

    public List<CategoryTreeResponse> getTree() {
        return null;
    }

    public CategoryNavigationResponse getNavigation(Long categoryId) {
        return null;
    }

    public CategoryResponse moveParent(Long categoryId, Long parentId) {
        return null;
    }

    public CategoryResponse updateCategory(Long categoryId, String name, String imageUrl) {
        return null;
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

    private Category findParentByCategory(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return findCategoryOrThrow(parentId);
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }
}
