package com.example.product_service.controller;

import com.example.product_service.common.advice.dto.ErrorResponse;
import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.controller.util.specification.annotation.*;
import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.request.ModifyCategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final SortFieldValidator sortFieldValidator;

    @AdminApi
    @Operation(summary = "카테고리 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = CategoryResponseDto.class)))
    @BadRequestApiResponse @ForbiddenApiResponse @ConflictApiResponse
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Validated CategoryRequestDto categoryRequestDto){
        CategoryResponseDto category = categoryService.saveCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @Operation(summary = "루트 카테고리 리스트 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponseDto.class))))
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories(){
        return ResponseEntity.ok(List.of(new CategoryResponseDto(1L, "의류", 2L, "http://localhost:9000.jpg")));
    }

    @Operation(summary = "자식 카테고리 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponseDto.class))))
    @NotFoundApiResponse
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponseDto>> getChildByCategoryId(@PathVariable("categoryId") Long categoryId){
        List<CategoryResponseDto> childCategories = categoryService.getChildCategories(categoryId);
        return ResponseEntity.ok(childCategories);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                                  @RequestBody @Validated ModifyCategoryRequestDto modifyCategoryRequestDto){

        if(Objects.equals(categoryId, modifyCategoryRequestDto.getParentId())){
            throw new BadRequestException("An item cannot be set as its own parent");
        }
        CategoryResponseDto category = categoryService.modifyCategory(categoryId, modifyCategoryRequestDto);
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
    public ResponseEntity<PageDto<CategoryResponseDto>> getMainCategoryList(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){

        sortFieldValidator.validateSortFields(pageable.getSort(), Categories.class, null);
        PageDto<CategoryResponseDto> pageDto = categoryService.getRootCategories(pageable);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/{categoryId}/root")
    public ResponseEntity<CategoryResponseDto> getRootByCategoryId(@PathVariable("categoryId") Long categoryId) {
        CategoryResponseDto rootCategoryDetailsOf = categoryService.getRootCategoryDetailsOf(categoryId);
        return ResponseEntity.ok(rootCategoryDetailsOf);
    }
}
