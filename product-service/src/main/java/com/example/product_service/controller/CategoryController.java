package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.*;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.ModifyCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponseDto;
import com.example.product_service.exception.BadRequestException;
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
import java.util.Objects;


@RestController
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관련 API")
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @AdminApi
    @Operation(summary = "카테고리 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @ConflictApiResponse
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Validated CategoryRequest categoryRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryResponseDto());
    }

    @Operation(summary = "루트 카테고리 리스트 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories(){
        return ResponseEntity.ok(List.of(new CategoryResponseDto(1L, "의류", 2L, "http://localhost:9000.jpg")));
    }

    @Operation(summary = "자식 카테고리 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponseDto>> getChildByCategoryId(@PathVariable("categoryId") Long categoryId){
        List<CategoryResponseDto> childCategories = categoryService.getChildCategories(categoryId);
        return ResponseEntity.ok(childCategories);
    }

    @Operation(summary = "카테고리 계층 구조 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{categoryId}/hierarchy")
    public ResponseEntity<CategoryHierarchyResponse> getHierarchyByCategoryId(@PathVariable("categoryId") Long categoryId){
        return ResponseEntity.ok(new CategoryHierarchyResponse());
    }

    @AdminApi
    @Operation(summary = "카테고리 수정")
    @ApiResponse(responseCode = "200", description = "카테고리 수정")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                                  @RequestBody @Validated ModifyCategoryRequest request){

        if(Objects.equals(categoryId, request.getParentId())){
            throw new BadRequestException("An item cannot be set as its own parent");
        }
        CategoryResponseDto category = categoryService.modifyCategory(categoryId, request);
        return ResponseEntity.ok(category);
    }

    @AdminApi
    @Operation(summary = "카테고리 삭제")
    @ApiResponse(responseCode = "204", description = "카테고리 삭제")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
