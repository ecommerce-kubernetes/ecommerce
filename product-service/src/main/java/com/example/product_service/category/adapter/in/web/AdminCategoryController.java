package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryIdResponse;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories/admin")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryIdResponse> saveCategory(@RequestBody @Validated CreateCategoryRequest request) {
        CategoryCommand.Create command = request.toCommand();
        CategoryResult.Detail result = categoryService.saveCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryIdResponse.from(result));
    }

    @PatchMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryIdResponse> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                             @RequestBody @Validated UpdateCategoryRequest request) {
        CategoryCommand.Update command = request.toCommand(categoryId);
        CategoryResult.Detail result = categoryService.updateCategory(command);
        return ResponseEntity.ok(CategoryIdResponse.from(result));
    }

    @PatchMapping("/categories/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryIdResponse> moveParent(@PathVariable("categoryId") Long categoryId,
                                                         @RequestBody @Validated MoveCategoryRequest request) {
        CategoryResult.Detail result = categoryService.moveParent(categoryId, request.newParentId());
        return ResponseEntity.ok(CategoryIdResponse.from(result));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
