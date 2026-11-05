package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.response.ChildCategoriesResponse;
import com.example.product_service.category.adapter.in.web.dto.response.DetailCategoryResponse;
import com.example.product_service.category.adapter.in.web.dto.response.RootCategoriesResponse;
import com.example.product_service.category.adapter.in.web.dto.response.TreeCategoriesResponse;
import com.example.product_service.category.application.service.CategoryQueryService;
import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.example.product_service.category.application.service.dto.result.DetailCategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.category.application.service.dto.result.TreeCategoriesResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    @GetMapping("/roots")
    public ResponseEntity<RootCategoriesResponse> getRootCategories() {
        RootCategoriesResult roots = categoryQueryService.getRoots();
        return ResponseEntity.ok(RootCategoriesResponse.from(roots));
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<ChildCategoriesResponse> getCategoryChildren(@PathVariable("categoryId") Long categoryId) {
        ChildCategoriesResult children = categoryQueryService.getChildren(categoryId);
        return ResponseEntity.ok(ChildCategoriesResponse.from(children));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<DetailCategoryResponse> getCategory(@PathVariable("categoryId") Long categoryId) {
        DetailCategoryResult detail = categoryQueryService.getDetail(categoryId);
        return ResponseEntity.ok(DetailCategoryResponse.from(detail));
    }

    @GetMapping("/tree")
    public ResponseEntity<TreeCategoriesResponse> getCategoryTree() {
        TreeCategoriesResult tree = categoryQueryService.getTree();
        return ResponseEntity.ok(TreeCategoriesResponse.from(tree));
    }
}
