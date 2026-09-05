package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.response.ChildCategoriesResponse;
import com.example.product_service.category.adapter.in.web.dto.response.DetailCategoryResponse;
import com.example.product_service.category.adapter.in.web.dto.response.RootCategoriesResponse;
import com.example.product_service.category.adapter.in.web.dto.response.TreeCategoriesResponse;
import com.example.product_service.category.application.service.CategoryQueryService;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryQueryService categoryQueryService;
    private final CategoryService categoryService;

    @GetMapping("/roots")
    public ResponseEntity<RootCategoriesResponse> getRootCategories() {
        RootCategoriesResult roots = categoryQueryService.getRoots();
        return ResponseEntity.ok(RootCategoriesResponse.from(roots));
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<ChildCategoriesResponse> getCategoryChildren(@PathVariable("categoryId") Long categoryId) {
        List<CategoryResultDeprecated.Tree> tree = categoryService.getTree();
        return ResponseEntity.ok(ChildCategoriesResponse.from(tree));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<DetailCategoryResponse> getCategory(@PathVariable("categoryId") Long categoryId) {
        CategoryResultDeprecated.Navigation result = categoryService.getNavigation(categoryId);
        return ResponseEntity.ok(DetailCategoryResponse.from(result));
    }

    @GetMapping("/tree")
    public ResponseEntity<TreeCategoriesResponse> getCategoryTree() {
        List<CategoryResultDeprecated.Tree> results = categoryService.getTree();
        return ResponseEntity.ok(TreeCategoriesResponse.from(results));
    }
}
