package com.example.product_service.controller;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Validated CategoryRequestDto categoryRequestDto){
        CategoryResponseDto category = categoryService.saveCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> updateCategoryName(@PathVariable("categoryId") Long categoryId,
                                                                  @RequestBody @Validated CategoryRequestDto categoryRequestDto){
        CategoryResponseDto category = categoryService.modifyCategory(categoryId, categoryRequestDto);
        return ResponseEntity.ok(category);
    }

}
