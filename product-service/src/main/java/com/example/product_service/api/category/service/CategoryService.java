package com.example.product_service.api.category.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryResponse saveCategory(String name, Long parentId, String imageUrl) {
        String trimName = name.trim();
        Category parent = getValidatedParent(parentId);
        validateDuplicateName(parent, trimName);
        Category category = Category.create(trimName, parent, imageUrl);
        categoryRepository.save(category);
        category.generatePath();
        return CategoryResponse.from(category);
    }

    public CategoryResponse getCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        return CategoryResponse.from(category);
    }

    public List<CategoryTreeResponse> getTree() {
        Sort sort = Sort.by(Sort.Direction.ASC, "depth", "id");
        List<Category> allCategories = categoryRepository.findAll(sort);
        return CategoryTreeResponse.convertTree(allCategories);
    }

    public CategoryNavigationResponse getNavigation(Long categoryId) {
        Category target = findCategoryOrThrow(categoryId);
        CategoryResponse current = CategoryResponse.from(target);
        List<CategoryResponse> ancestors = findCategoryPath(target);
        List<CategoryResponse> siblings = findSiblings(target);
        List<CategoryResponse> children = findChildren(target);
        return CategoryNavigationResponse.of(current, ancestors, siblings, children);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, String name, String imageUrl) {
        Category category = findCategoryOrThrow(categoryId);
        if (StringUtils.hasText(name) && !category.getName().equals(name)) {
            validateDuplicateName(category.getParent(), name);
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
        Category newParent = resolveNewParent(parentId, isRoot);

        if (newParent != null) {
            validateHierarchy(category, newParent);
            validateParentHasProducts(newParent);
        }

        validateDuplicateName(newParent, category.getName());
        category.moveParent(newParent);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        if (!category.getChildren().isEmpty()){
            throw new BusinessException(CategoryErrorCode.HAS_CHILD);
        }
        if (productRepository.existsByCategoryId(category.getId())){
            throw new BusinessException(CategoryErrorCode.HAS_PRODUCT);
        }
        categoryRepository.delete(category);
    }

    private Category resolveNewParent(Long parentId, Boolean isRoot) {
        if (Boolean.TRUE.equals(isRoot)) {
            return null;
        }
        return findCategoryOrThrow(parentId);
    }

    private void validateHierarchy(Category target, Category newParent) {
        if (target.getId().equals(newParent.getId())){
            throw new BusinessException(CategoryErrorCode.INVALID_HIERARCHY);
        }

        if (newParent.getPath().startsWith(target.getPath() + "/")) {
            throw new BusinessException(CategoryErrorCode.INVALID_HIERARCHY);
        }
    }

    // 부모 검증
    private void validateParentHasProducts(Category parent) {
        // 부모 카테고리에 속한 상품이 존재하면 예외를 던짐
        if (productRepository.existsByCategoryId(parent.getId())) {
            throw new BusinessException(CategoryErrorCode.HAS_PRODUCT);
        }
    }

    // 형제중 같은 이름이 존재하면 예외를 던짐
    private void validateDuplicateName(Category parent, String name) {
        Long parentId = (parent == null) ? null : parent.getId();
        if (categoryRepository.existsDuplicateName(parentId, name)) {
            throw new BusinessException(CategoryErrorCode.DUPLICATE_NAME);
        }
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    private List<CategoryResponse> findCategoryPath(Category current) {
        if (current.isRoot()) {
            return List.of(CategoryResponse.from(current));
        }
        List<Long> ancestorIds = current.getPathIds();
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
        return createCategoryResponses(current.getChildren());
    }

    private List<CategoryResponse> createCategoryResponses(List<Category> categories) {
        return categories.stream().map(CategoryResponse::from).toList();
    }

    // 부모 카테고리를 찾고 부모 카테고리에 속한 상품이 존재하는지 검증
    private Category getValidatedParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        Category parent = findCategoryOrThrow(parentId);
        if (productRepository.existsByCategoryId(parent.getId())) {
            throw new BusinessException(CategoryErrorCode.HAS_PRODUCT);
        }
        return parent;
    }
}
