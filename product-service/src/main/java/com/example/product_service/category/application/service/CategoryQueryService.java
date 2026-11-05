package com.example.product_service.category.application.service;

import com.example.product_service.category.application.port.CategoryRepository;
import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.example.product_service.category.application.service.dto.result.DetailCategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.category.application.service.dto.result.TreeCategoriesResult;
import com.example.product_service.category.domain.Category;
import com.example.product_service.category.exception.CategoryErrorCode;
import com.example.product_service.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public RootCategoriesResult getRoots() {
        List<Category> roots = categoryRepository.findAllByParentIsNull();
        return RootCategoriesResult.from(roots);
    }

    public ChildCategoriesResult getChildren(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        List<Category> children = categoryRepository.findAllByParentId(categoryId);
        return ChildCategoriesResult.from(children);
    }

    public DetailCategoryResult getDetail(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        List<Long> pathIds = category.getAncestorIds();

        List<Category> pathCategories = categoryRepository.findAllById(pathIds);

        return DetailCategoryResult.from(category, pathCategories);
    }

    public TreeCategoriesResult getTree() {
        List<Category> allCategories = categoryRepository.findAll();
        return TreeCategoriesResult.from(allCategories);
    }

}
