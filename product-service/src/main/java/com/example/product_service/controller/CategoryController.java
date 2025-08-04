package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.*;
import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.request.ModifyCategoryRequestDto;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
//        CategoryResponseDto category = categoryService.saveCategory(categoryRequestDto);
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
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                                  @RequestBody @Validated ModifyCategoryRequestDto modifyCategoryRequestDto){

        if(Objects.equals(categoryId, modifyCategoryRequestDto.getParentId())){
            throw new BadRequestException("An item cannot be set as its own parent");
        }
        CategoryResponseDto category = categoryService.modifyCategory(categoryId, modifyCategoryRequestDto);
        return ResponseEntity.ok(category);
    }

    @AdminApi
    @Operation(summary = "카테고리 삭제")
    @ApiResponse(responseCode = "204", description = "카테고리 삭제")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> removeCategory(@PathVariable("categoryId") Long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }


    //TODO 삭제 예정
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable("categoryId") Long categoryId){
        CategoryResponseDto categoryDetails = categoryService.getCategoryDetails(categoryId);
        return ResponseEntity.ok(categoryDetails);
    }

    //TODO 삭제 예정
    @GetMapping
    public ResponseEntity<PageDto<CategoryResponseDto>> getMainCategoryList(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){

//        sortFieldValidator.validateSortFields(pageable.getSort(), Categories.class, null);
        PageDto<CategoryResponseDto> pageDto = categoryService.getRootCategories(pageable);
        return ResponseEntity.ok(pageDto);
    }

    //TODO 삭제 예정
    @GetMapping("/{categoryId}/root")
    public ResponseEntity<CategoryResponseDto> getRootByCategoryId(@PathVariable("categoryId") Long categoryId) {
        CategoryResponseDto rootCategoryDetailsOf = categoryService.getRootCategoryDetailsOf(categoryId);
        return ResponseEntity.ok(rootCategoryDetailsOf);
    }
}
