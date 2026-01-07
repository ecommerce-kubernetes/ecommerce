package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

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
}
