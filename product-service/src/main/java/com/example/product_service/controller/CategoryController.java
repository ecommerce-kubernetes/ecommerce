package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.*;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관련 API")
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @AdminApi
    @Operation(summary = "카테고리 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @ConflictApiResponse @NotFoundApiResponse
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Validated CategoryRequest request){
        CategoryResponse response = categoryService.saveCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "루트 카테고리 리스트 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories(){
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "자식 카테고리 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildrenCategories(@PathVariable("categoryId") Long categoryId){
        List<CategoryResponse> childCategories = categoryService.getChildrenCategoriesById(categoryId);
        return ResponseEntity.ok(childCategories);
    }

    @Operation(summary = "카테고리 계층 구조 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{categoryId}/hierarchy")
    public ResponseEntity<CategoryHierarchyResponse> getHierarchy(@PathVariable("categoryId") Long categoryId){
        CategoryHierarchyResponse response = categoryService.getHierarchyByCategoryId(categoryId);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "카테고리 수정")
    @ApiResponse(responseCode = "200", description = "카테고리 수정")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse @ConflictApiResponse
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                           @RequestBody @Validated UpdateCategoryRequest request){
        CategoryResponse category = categoryService.updateCategoryById(categoryId, request);
        return ResponseEntity.ok(category);
    }

    @AdminApi
    @Operation(summary = "카테고리 삭제")
    @ApiResponse(responseCode = "204", description = "카테고리 삭제")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId){
        categoryService.deleteCategoryById(categoryId);
        return ResponseEntity.noContent().build();
    }
}
