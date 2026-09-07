package com.example.product_service.category.application.port;

import com.example.product_service.category.domain.Category;

import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(Long id);
    boolean existsByParentIdAndName(Long parentId, String name);
}
