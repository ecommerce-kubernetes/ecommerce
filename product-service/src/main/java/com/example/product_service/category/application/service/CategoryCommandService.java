package com.example.product_service.category.application.service;

import com.example.product_service.category.application.port.CategoryProductPort;
import com.example.product_service.category.application.port.CategoryRepository;
import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;
import com.example.product_service.category.application.service.dto.command.UpdateCategoryCommand;
import com.example.product_service.category.domain.Category;
import com.example.product_service.category.exception.CategoryErrorCode;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandService {

    private final IdGenerator idGenerator;

    private final CategoryRepository categoryRepository;

    private final CategoryProductPort categoryProductPort;

    public Long createCategory(CreateCategoryCommand command) {
        Category category = command.parentId() != null
                ? createChildCategory(command)
                : createRootCategory(command);

        return categoryRepository.save(category).getId();
    }

    private Category createRootCategory(CreateCategoryCommand command) {
        validateSiblingNameNotDuplicated(null, command.name());
        return Category.createRoot(idGenerator.generate(), command.name(), command.imagePath());
    }

    private Category createChildCategory(CreateCategoryCommand command) {
        Category parent = categoryRepository.findById(command.parentId())
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        validateSiblingNameNotDuplicated(parent.getId(), command.name());

        if (categoryProductPort.existsProductForCategory(parent.getId())) {
            throw new BusinessException(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);
        }

        return Category.createChild(idGenerator.generate(), command.name(), command.imagePath(), parent);
    }

    private void validateSiblingNameNotDuplicated(Long parentId, String name) {
        if (categoryRepository.existsByParentIdAndName(parentId, name)) {
            throw new BusinessException(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
        }
    }

    public Long updateCategory(UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(command.id())
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getName().equals(command.name())) {
            Long parentId = category.getParent() != null ? category.getParent().getId() : null;
            if (categoryRepository.existsByParentIdAndNameAndIdNot(parentId, command.name(), category.getId())) {
                throw new BusinessException(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
            }
        }

        category.update(command.name(), command.imagePath());
        return category.getId();
    }

    public Long moveParent(Long categoryId, Long newParentId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        Category newParent = resolveParent(newParentId);

        validateMovable(category, newParent);

        String oldPath = category.getPath();
        int oldDepth = category.getDepth();

        category.moveParent(newParent);

        relocateDescendants(category, oldPath, oldDepth);

        return category.getId();
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateMovable(Category category, Category newParent) {
        Long newParentId = newParent != null ? newParent.getId() : null;

        if (categoryRepository.existsByParentIdAndNameAndIdNot(newParentId, category.getName(), category.getId())) {
            throw new BusinessException(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
        }

        if (newParent != null && categoryProductPort.existsProductForCategory(newParent.getId())) {
            throw new BusinessException(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);
        }
    }

    private void relocateDescendants(Category category, String oldPath, int oldDepth) {
        int depthDelta = category.getDepth() - oldDepth;
        if (depthDelta == 0 && category.getPath().equals(oldPath)) {
            return;
        }

        List<Category> descendants = categoryRepository.findAllByPathStartingWith(oldPath + "/");
        descendants.forEach(descendant -> descendant.relocatePrefix(oldPath, category.getPath(), depthDelta));
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isLeaf()) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_HAS_CHILD);
        }

        if (categoryProductPort.existsProductForCategory(categoryId)) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_HAS_PRODUCT);
        }

        categoryRepository.delete(category);
    }
}
