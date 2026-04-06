package com.example.product_service.api.category.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.category.service.dto.command.CategoryCommand;
import com.example.product_service.api.category.service.dto.result.CategoryResult;
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

    public CategoryResult.Detail getCategory(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        return CategoryResult.Detail.from(category);
    }

    public List<CategoryResult.Tree> getTree() {
        Sort sort = Sort.by(Sort.Direction.ASC, "depth", "id");
        List<Category> allCategories = categoryRepository.findAll(sort);
        return convertTree(allCategories);
    }

    public CategoryResult.Navigation getNavigation(Long categoryId) {
        Category target = findCategoryOrThrow(categoryId);
        CategoryResult.Detail current = CategoryResult.Detail.from(target);
        List<CategoryResult.Detail> path = findCategoryPath(target);
        List<CategoryResult.Detail> siblings = findSiblings(target);
        List<CategoryResult.Detail> children = findChildren(target);
        return CategoryResult.Navigation.builder()
                .current(current)
                .path(path)
                .siblings(siblings)
                .children(children)
                .build();
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
    public CategoryResult.Detail moveParent(Long categoryId, Long parentId) {
        // 카테고리 조회
        Category category = findCategoryOrThrow(categoryId);
        Category parent = getValidatedParent(parentId);
        validateDuplicateName(parent, category.getName());
        category.moveParent(parent);
        return CategoryResult.Detail.from(category);
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

    private List<CategoryResult.Detail> findCategoryPath(Category current) {
        if (current.isRoot()) {
            return List.of(CategoryResult.Detail.from(current));
        }
        List<Long> ancestorIds = current.getPathIds();
        List<Category> ancestors = categoryRepository.findByInOrderDepth(ancestorIds);
        return createCategoryResponses(ancestors);
    }

    private List<CategoryResult.Detail> findSiblings(Category current) {
        if (current.isRoot()) {
            List<Category> siblings = categoryRepository.findByParentIsNull();
            return createCategoryResponses(siblings);
        }
        List<Category> siblings = categoryRepository.findByParentId(current.getParent().getId());
        return createCategoryResponses(siblings);
    }

    private List<CategoryResult.Detail> findChildren(Category current) {
        return createCategoryResponses(current.getChildren());
    }

    private List<CategoryResult.Tree> convertTree(List<Category> categories) {
        List<CategoryResult.Tree> allDtoList = categories.stream().map(CategoryResult.Tree::from)
                .toList();
        Map<Long, CategoryResult.Tree> dtoMap = allDtoList.stream().collect(Collectors.toMap(CategoryResult.Tree::getId, Function.identity()));
        List<CategoryResult.Tree> rootCategories = new ArrayList<>();
        for (CategoryResult.Tree categoryResult : allDtoList) {
            if (categoryResult.getDepth() == 1) {
                rootCategories.add(categoryResult);
            } else {
                CategoryResult.Tree parent = dtoMap.get(categoryResult.getParentId());
                parent.addChild(categoryResult);
            }
        }
        return rootCategories;
    }

    private List<CategoryResult.Detail> createCategoryResponses(List<Category> categories) {
        return categories.stream().map(CategoryResult.Detail::from).toList();
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
