package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/tree")
    public ResponseEntity<CategoryTreeResponse> getCategoryTree(){
        CategoryTreeResponse response = categoryService.getTree();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/navigation")
    public ResponseEntity<CategoryNavigationResponse> getCategoryNavigation() {
        CategoryNavigationResponse response = categoryService.getNavigation();
        return ResponseEntity.ok(response);
    }
}
