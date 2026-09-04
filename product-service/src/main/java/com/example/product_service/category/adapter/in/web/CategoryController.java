package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.response.CategoryDetailResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryListResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryTreeListResponse;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.CategoryErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/roots")
    public ResponseEntity<CategoryListResponse> getRootCategories() {
        List<CategoryResult.Tree> roots = categoryService.getTree();
        return ResponseEntity.ok(CategoryListResponse.fromRoots(roots));
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<CategoryListResponse> getCategoryChildren(@PathVariable("categoryId") Long categoryId) {
        List<CategoryResult.Tree> tree = categoryService.getTree();
        CategoryResult.Tree target = findInTree(tree, categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        return ResponseEntity.ok(CategoryListResponse.fromChildren(target));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDetailResponse> getCategory(@PathVariable("categoryId") Long categoryId) {
        CategoryResult.Navigation result = categoryService.getNavigation(categoryId);
        return ResponseEntity.ok(CategoryDetailResponse.from(result));
    }

    @GetMapping("/tree")
    public ResponseEntity<CategoryTreeListResponse> getCategoryTree() {
        List<CategoryResult.Tree> results = categoryService.getTree();
        return ResponseEntity.ok(CategoryTreeListResponse.from(results));
    }


    private Optional<CategoryResult.Tree> findInTree(List<CategoryResult.Tree> nodes, Long categoryId) {
        for (CategoryResult.Tree node : nodes) {
            if (node.getId().equals(categoryId)) {
                return Optional.of(node);
            }
            Optional<CategoryResult.Tree> found = findInTree(node.getChildren(), categoryId);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }
}
