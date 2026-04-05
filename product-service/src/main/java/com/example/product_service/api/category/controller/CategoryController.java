package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.request.CategoryRequest.CreateRequest;
import com.example.product_service.api.category.controller.dto.request.CategoryRequest.MoveRequest;
import com.example.product_service.api.category.controller.dto.request.CategoryRequest.UpdateRequest;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse.Detail;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse.Navigation;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse.Tree;
import com.example.product_service.api.category.service.CategoryService;
import com.example.product_service.api.category.service.dto.command.CategoryCommand;
import com.example.product_service.api.category.service.dto.command.CategoryCommand.Create;
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
    public ResponseEntity<Detail> saveCategory(@RequestBody @Validated CreateRequest request) {
        Create command = request.toCommand();
        CategoryResult result = categoryService.saveCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Detail.from(result));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Tree>> getCategoryTree(){
        List<CategoryTreeResult> results = categoryService.getTree();
        List<Tree> responses = results.stream().map(Tree::from).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/navigation/{categoryId}")
    public ResponseEntity<Navigation> getCategoryNavigation(@PathVariable("categoryId") Long categoryId) {
        CategoryNavigationResult result = categoryService.getNavigation(categoryId);
        return ResponseEntity.ok(Navigation.from(result));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Detail> getCategory(@PathVariable("categoryId") Long categoryId){
        CategoryResult result = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(Detail.from(result));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Detail> updateCategory(@PathVariable("categoryId") Long categoryId,
                                                         @RequestBody @Validated UpdateRequest request) {
        CategoryResult result = categoryService.updateCategory(categoryId, request.name(), request.imagePath());
        return ResponseEntity.ok(Detail.from(result));
    }

    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Detail> moveParent(@PathVariable("categoryId") Long categoryId,
                                                     @RequestBody @Validated MoveRequest request) {
        CategoryResult result = categoryService.moveParent(categoryId, request.parentId());
        return ResponseEntity.ok(Detail.from(result));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
