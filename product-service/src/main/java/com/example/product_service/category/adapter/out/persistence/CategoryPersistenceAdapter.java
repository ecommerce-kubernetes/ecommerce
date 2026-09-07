package com.example.product_service.category.adapter.out.persistence;

import com.example.product_service.category.application.port.CategoryRepository;
import com.example.product_service.category.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id);
    }

    @Override
    public boolean existsByParentIdAndName(Long parentId, String name) {
        return categoryJpaRepository.existsByParentIdAndName(parentId, name);
    }
}
