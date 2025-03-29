package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final SortFieldValidator sortFieldValidator;

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

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> removeCategory(@PathVariable("categoryId") Long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable("categoryId") Long categoryId){
        CategoryResponseDto categoryDetails = categoryService.getCategoryDetails(categoryId);
        return ResponseEntity.ok(categoryDetails);
    }

    @GetMapping
    public ResponseEntity<PageDto<CategoryResponseDto>> getCategories(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){

        sortFieldValidator.validateSortFields(pageable.getSort(), Categories.class);
        PageDto<CategoryResponseDto> pageDto = categoryService.getCategoryList(pageable);
        return ResponseEntity.ok(pageDto);
    }
}
