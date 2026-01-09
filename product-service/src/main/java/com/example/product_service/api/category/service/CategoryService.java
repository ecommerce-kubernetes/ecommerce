package com.example.product_service.api.category.service;

import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private static final int MAX_DEPTH = 5;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public CategoryResponse saveCategory(String name, Long parentId, String imageUrl) {
        Category parent = findParentByCategory(parentId);
        validationCategory(parent, name);
        Category category = Category.create(name, parent, imageUrl);
        categoryRepository.save(category);
        category.generatePath();
        return CategoryResponse.from(category);
    }

    public CategoryResponse getCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        return CategoryResponse.from(category);
    }

    public List<CategoryTreeResponse> getTree() {
        List<Category> allCategories = categoryRepository.findAll();
        return mappingCategoryTreeResponse(allCategories);
    }

    public CategoryNavigationResponse getNavigation(Long categoryId) {
        Category target = findCategoryOrThrow(categoryId);
        CategoryResponse current = CategoryResponse.from(target);
        List<CategoryResponse> ancestors = findAncestors(target);
        List<CategoryResponse> siblings = findSiblings(target);
        List<CategoryResponse> children = findChildren(target);
        return CategoryNavigationResponse.of(current, ancestors, siblings, children);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, String name, String imageUrl) {
        Category category = findCategoryOrThrow(categoryId);
        if (StringUtils.hasText(name) && !category.getName().equals(name)) {
            validationDuplicateName(category.getParent(), name);
            category.rename(name);
        }

        if (StringUtils.hasText(imageUrl)) {
            category.changeImage(imageUrl);
        }
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse moveParent(Long categoryId, Long parentId, Boolean isRoot) {
        Category category = findCategoryOrThrow(categoryId);
        if (isRoot) {
            category.moveParent(null);
        } else {
            Category parent = findCategoryOrThrow(parentId);
            validationCategory(parent, category.getName());
            category.moveParent(parent);
        }
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findByIdOrThrow(categoryId);
        if (!category.getChildren().isEmpty()){
            throw new BusinessException(CategoryErrorCode.HAS_CHILD);
        }
        if (productRepository.existsByCategoryId(category.getId())){
            throw new BusinessException(CategoryErrorCode.HAS_PRODUCT);
        }
        categoryRepository.delete(category);
    }

    private void validationCategory(Category parent, String name) {
        if (parent != null) {
            // 더이상 자식 카테고리를 추가할 수 없는 경우 예외를 던짐
            if (parent.getDepth() >= MAX_DEPTH){
                throw new BusinessException(CategoryErrorCode.EXCEED_MAX_DEPTH);
            }
            // 부모 카테고리에 속한 상품이 존재하는 경우 예외를 던짐
            if (productRepository.existsByCategoryId(parent.getId())){
                throw new BusinessException(CategoryErrorCode.HAS_PRODUCT);
            }
        }
        validationDuplicateName(parent, name);
    }

    // 형제중 같은 이름이 존재하면 예외를 던짐
    private void validationDuplicateName(Category parent, String name) {
        Long parentId = (parent == null) ? null : parent.getId();
        if (categoryRepository.existsDuplicateName(parentId, name)) {
            throw new BusinessException(CategoryErrorCode.DUPLICATE_NAME);
        }
    }

    private Category findByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
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

    private List<CategoryTreeResponse> mappingCategoryTreeResponse(List<Category> allCategories) {
        List<CategoryTreeResponse> allDtoList = allCategories.stream().map(CategoryTreeResponse::from).toList();
        Map<Long, CategoryTreeResponse> dtoMap = allDtoList.stream().collect(Collectors.toMap(CategoryTreeResponse::getId, Function.identity()));
        List<CategoryTreeResponse> rootCategories = new ArrayList<>();
        for (CategoryTreeResponse category : allDtoList) {
            // depth 가 1 이면 최상위 카테고리
            if (category.getDepth() == 1) {
                rootCategories.add(category);
            } else {
                // depth 가 1 이상이면 map에서 부모 카테고리를 찾아 addChild() 메서드 호출
                CategoryTreeResponse parent = dtoMap.get(category.getParentId());
                parent.addChild(category);
            }
        }
        return rootCategories;
    }

    private List<CategoryResponse> findAncestors(Category current) {
        if (current.isRoot()) {
            return List.of(CategoryResponse.from(current));
        }
        List<Long> ancestorIds = current.getAncestorsIds();
        List<Category> ancestors = categoryRepository.findByInOrderDepth(ancestorIds);
        return createCategoryResponses(ancestors);
    }

    private List<CategoryResponse> findSiblings(Category current) {
        if (current.isRoot()) {
            List<Category> siblings = categoryRepository.findByParentIsNull();
            return createCategoryResponses(siblings);
        }
        List<Category> siblings = categoryRepository.findByParentId(current.getParent().getId());
        return createCategoryResponses(siblings);
    }

    private List<CategoryResponse> findChildren(Category current) {
        List<Category> children = categoryRepository.findByParentId(current.getId());
        return createCategoryResponses(children);
    }
    private List<CategoryResponse> createCategoryResponses(List<Category> categories) {
        return categories.stream().map(CategoryResponse::from).toList();
    }
}
