package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.api.category.controller.dto.MoveCategoryRequest;
import com.example.product_service.api.category.controller.dto.UpdateCategoryRequest;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> saveCategory(@RequestBody @Validated CategoryRequest request) {
        CategoryResponse response = categoryService.saveCategory(request.getName(), request.getParentId(), request.getImageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree(){
        List<CategoryTreeResponse> response = categoryService.getTree();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/navigation/{categoryId}")
    public ResponseEntity<CategoryNavigationResponse> getCategoryNavigation(@PathVariable("categoryId") Long categoryId) {
        CategoryNavigationResponse response = categoryService.getNavigation(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable("categoryId") Long categoryId){
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                           @RequestBody @Validated UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request.getName(), request.getImageUrl());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> moveParent(@PathVariable("categoryId") Long categoryId,
                                                       @RequestBody @Validated MoveCategoryRequest request) {
        CategoryResponse response = categoryService.moveParent(categoryId, request.getParentId(), request.getIsRoot());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
