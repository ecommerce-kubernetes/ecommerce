package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.response.CreateCategoryResponse;
import com.example.product_service.category.adapter.in.web.dto.response.MoveCategoryResponse;
import com.example.product_service.category.adapter.in.web.dto.response.UpdateCategoryResponse;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateCategoryResponse> saveCategory(@RequestBody @Validated CreateCategoryRequest request) {
        CategoryCommand.Create command = request.toCommand();
        Long id = categoryService.saveCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateCategoryResponse.of(id));
    }

    @PatchMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                                 @RequestBody @Validated UpdateCategoryRequest request) {
        CategoryCommand.Update command = request.toCommand(categoryId);
        Long id = categoryService.updateCategory(command);
        return ResponseEntity.ok(UpdateCategoryResponse.of(id));
    }

    @PatchMapping("/categories/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MoveCategoryResponse> moveParent(@PathVariable("categoryId") Long categoryId,
                                                           @RequestBody @Validated MoveCategoryRequest request) {
        Long id = categoryService.moveParent(categoryId, request.newParentId());
        return ResponseEntity.ok(MoveCategoryResponse.of(id));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
