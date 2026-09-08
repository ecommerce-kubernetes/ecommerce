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
                : Category.createRoot(idGenerator.generate(), command.name(), command.imagePath());

        return categoryRepository.save(category).getId();
    }

    private Category createChildCategory(CreateCategoryCommand command) {
        Category parent = categoryRepository.findById(command.parentId())
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByParentIdAndName(parent.getId(), command.name())) {
            throw new BusinessException(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
        }

        if (categoryProductPort.existsProductForCategory(parent.getId())) {
            throw new BusinessException(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);
        }

        return Category.createChild(idGenerator.generate(), command.name(), command.imagePath(), parent);
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

    public Long moveParent(Long categoryId, Long parentId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        Category parent = parentId != null ? categoryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND)) : null;

        if (parent != null) {
            if (categoryRepository.existsByParentIdAndName(parent.getId(), category.getName())) {
                throw new BusinessException(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
            }

            if (categoryProductPort.existsProductForCategory(parentId)) {
                throw new BusinessException(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);
            }
        }

        category.moveParent(parent);

        return category.getId();
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isLeaf()) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_HAS_CHILD);
        }

        if (categoryProductPort.existsProductForCategory(categoryId)) {
            throw new BusinessException(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);
        }

        categoryRepository.delete(category);
    }
}
