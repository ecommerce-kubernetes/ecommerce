package com.example.product_service.category.application.service;

import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.example.product_service.category.application.service.dto.result.DetailCategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.category.application.service.dto.result.TreeCategoriesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    public RootCategoriesResult getRoots() {
        return null;
    }

    public ChildCategoriesResult getChildren(Long categoryId) {
        return null;
    }

    public DetailCategoryResult getDetail(Long categoryId) {
        return null;
    }

    public TreeCategoriesResult getTree() {
        return null;
    }
}
