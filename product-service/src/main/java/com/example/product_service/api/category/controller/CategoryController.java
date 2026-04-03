package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.request.CategoryRequest.CreateRequest;
import com.example.product_service.api.category.controller.dto.request.CategoryRequest.MoveRequest;
import com.example.product_service.api.category.controller.dto.request.CategoryRequest.UpdateRequest;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse.Detail;
import com.example.product_service.api.category.service.CategoryService;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResult;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
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
    public ResponseEntity<Detail> saveCategory(@RequestBody @Validated CreateRequest request) {
        CategoryResult result = categoryService.saveCategory(request.name(), request.parentId(), request.imagePath());
        return ResponseEntity.status(HttpStatus.CREATED).body(Detail.from(result));
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
    public ResponseEntity<CategoryResult> getCategory(@PathVariable("categoryId") Long categoryId){
        CategoryResult response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResult> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                         @RequestBody @Validated UpdateRequest request) {
        CategoryResult response = categoryService.updateCategory(categoryId, request.name(), request.imagePath());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResult> moveParent(@PathVariable("categoryId") Long categoryId,
                                                     @RequestBody @Validated MoveRequest request) {
        CategoryResult response = categoryService.moveParent(categoryId, request.parentId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
