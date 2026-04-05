package com.example.product_service.api.category.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.category.service.dto.command.CategoryCommand;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResult;
import com.example.product_service.api.category.service.dto.result.CategoryResult;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResult;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryResult.Detail saveCategory(CategoryCommand.Create command) {
        Category parent = getValidatedParent(command.parentId());
        validateDuplicateName(parent, command.name().trim());
        Category category = Category.create(command.name(), parent, command.imagePath());
        categoryRepository.save(category);
        category.generatePath();
        return CategoryResult.Detail.from(category);
    }

    public CategoryResult getCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        return CategoryResult.from(category);
    }

    public List<CategoryTreeResult> getTree() {
        Sort sort = Sort.by(Sort.Direction.ASC, "depth", "id");
        List<Category> allCategories = categoryRepository.findAll(sort);
        return convertTree(allCategories);
    }

    public CategoryNavigationResult getNavigation(Long categoryId) {
        Category target = findCategoryOrThrow(categoryId);
        CategoryResult current = CategoryResult.from(target);
        List<CategoryResult> ancestors = findCategoryPath(target);
        List<CategoryResult> siblings = findSiblings(target);
        List<CategoryResult> children = findChildren(target);
        return CategoryNavigationResult.of(current, ancestors, siblings, children);
    }

    @Transactional
    public CategoryResult.Detail updateCategory(CategoryCommand.Update command) {
        Category category = findCategoryOrThrow(command.id());
        if (StringUtils.hasText(command.name())) {
            validateDuplicateName(category.getParent(), command.name());
            category.rename(command.name());
        }

        if (command.imagePath() != null) {
            category.changeImage(command.imagePath());
        }
        return CategoryResult.Detail.from(category);
    }

    @Transactional
    public CategoryResult moveParent(Long categoryId, Long parentId) {
        // 카테고리 조회
        Category category = findCategoryOrThrow(categoryId);
        Category parent = getValidatedParent(parentId);
        validateDuplicateName(parent, category.getName());
        category.moveParent(parent);
        return CategoryResult.from(category);
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

    private List<CategoryResult> findCategoryPath(Category current) {
        if (current.isRoot()) {
            return List.of(CategoryResult.from(current));
        }
        List<Long> ancestorIds = current.getPathIds();
        List<Category> ancestors = categoryRepository.findByInOrderDepth(ancestorIds);
        return createCategoryResponses(ancestors);
    }

    private List<CategoryResult> findSiblings(Category current) {
        if (current.isRoot()) {
            List<Category> siblings = categoryRepository.findByParentIsNull();
            return createCategoryResponses(siblings);
        }
        List<Category> siblings = categoryRepository.findByParentId(current.getParent().getId());
        return createCategoryResponses(siblings);
    }

    private List<CategoryResult> findChildren(Category current) {
        return createCategoryResponses(current.getChildren());
    }

    private List<CategoryTreeResult> convertTree(List<Category> categories) {
        List<CategoryTreeResult> allDtoList = categories.stream().map(CategoryTreeResult::from)
                .toList();
        Map<Long, CategoryTreeResult> dtoMap = allDtoList.stream().collect(Collectors.toMap(CategoryTreeResult::getId, Function.identity()));
        List<CategoryTreeResult> rootCategories = new ArrayList<>();
        for (CategoryTreeResult categoryResult : allDtoList) {
            if (categoryResult.getDepth() == 1) {
                rootCategories.add(categoryResult);
            } else {
                CategoryTreeResult parent = dtoMap.get(categoryResult.getParentId());
                parent.addChild(categoryResult);
            }
        }
        return rootCategories;
    }

    private List<CategoryResult> createCategoryResponses(List<Category> categories) {
        return categories.stream().map(CategoryResult::from).toList();
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
