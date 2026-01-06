package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.api.category.controller.dto.MoveCategoryRequest;
import com.example.product_service.api.category.controller.dto.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/management/categories")
@PreAuthorize("hasRole('ADMIN')")
public class ManagementCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> saveCategory(@RequestBody @Validated CategoryRequest request) {
        CategoryResponse response = categoryService.saveCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                           @RequestBody @Validated UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request.getName(), request.getImageUrl());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{categoryId}/move")
    public ResponseEntity<CategoryResponse> moveParent(@PathVariable("categoryId") Long categoryId,
                                                       @RequestBody @Validated MoveCategoryRequest request) {
        CategoryResponse response = categoryService.moveParent(categoryId, request.getParentId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return ResponseEntity.noContent().build();
    }
}
