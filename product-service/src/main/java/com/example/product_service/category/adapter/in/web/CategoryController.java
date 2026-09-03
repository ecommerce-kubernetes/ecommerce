package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryDetailResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryNavigationResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryTreeResponse;
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

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDetailResponse> saveCategory(@RequestBody @Validated CreateCategoryRequest request) {
        CategoryCommand.Create command = request.toCommand();
        CategoryResult.Detail result = categoryService.saveCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryDetailResponse.from(result));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree(){
        List<CategoryResult.Tree> results = categoryService.getTree();
        List<CategoryTreeResponse> responses = CategoryTreeResponse.from(results);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/navigation/{categoryId}")
    public ResponseEntity<CategoryNavigationResponse> getCategoryNavigation(@PathVariable("categoryId") Long categoryId) {
        CategoryResult.Navigation result = categoryService.getNavigation(categoryId);
        return ResponseEntity.ok(CategoryNavigationResponse.from(result));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDetailResponse> getCategory(@PathVariable("categoryId") Long categoryId){
        CategoryResult.Detail result = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(CategoryDetailResponse.from(result));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDetailResponse> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                         @RequestBody @Validated UpdateCategoryRequest request) {
        CategoryCommand.Update command = request.toCommand(categoryId);
        CategoryResult.Detail result = categoryService.updateCategory(command);
        return ResponseEntity.ok(CategoryDetailResponse.from(result));
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDetailResponse> moveParent(@PathVariable("categoryId") Long categoryId,
                                                     @RequestBody @Validated MoveCategoryRequest request) {
        CategoryResult.Detail result = categoryService.moveParent(categoryId, request.parentId());
        return ResponseEntity.ok(CategoryDetailResponse.from(result));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
