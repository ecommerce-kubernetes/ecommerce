package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.request.CategoryRequest;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse.Tree;
import com.example.product_service.api.category.service.CategoryService;
import com.example.product_service.api.category.service.dto.command.CategoryCommand;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResult;
import com.example.product_service.api.category.service.dto.result.CategoryResult;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResult;
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
    public ResponseEntity<CategoryResponse.Detail> saveCategory(@RequestBody @Validated CategoryRequest.Create request) {
        CategoryCommand.Create command = request.toCommand();
        CategoryResult.Detail result = categoryService.saveCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.Detail.from(result));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse.Tree>> getCategoryTree(){
        List<CategoryTreeResult> results = categoryService.getTree();
        List<Tree> responses = results.stream().map(Tree::from).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/navigation/{categoryId}")
    public ResponseEntity<CategoryResponse.Navigation> getCategoryNavigation(@PathVariable("categoryId") Long categoryId) {
        CategoryNavigationResult result = categoryService.getNavigation(categoryId);
        return ResponseEntity.ok(CategoryResponse.Navigation.from(result));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse.Detail> getCategory(@PathVariable("categoryId") Long categoryId){
        CategoryResult.Detail result = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(CategoryResponse.Detail.from(result));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse.Detail> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                         @RequestBody @Validated CategoryRequest.Update request) {
        CategoryCommand.Update command = request.toCommand(categoryId);
        CategoryResult.Detail result = categoryService.updateCategory(command);
        return ResponseEntity.ok(CategoryResponse.Detail.from(result));
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse.Detail> moveParent(@PathVariable("categoryId") Long categoryId,
                                                     @RequestBody @Validated CategoryRequest.MoveRequest request) {
        CategoryResult.Detail result = categoryService.moveParent(categoryId, request.parentId());
        return ResponseEntity.ok(CategoryResponse.Detail.from(result));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
