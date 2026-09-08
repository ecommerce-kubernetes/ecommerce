package com.example.product_service.category.application.port;

import com.example.product_service.category.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(Long id);
    boolean existsByParentIdAndName(Long parentId, String name);
    boolean existsByParentIdAndNameAndIdNot(Long parentId, String name, Long id);
    boolean existsById(Long id);
    List<Category> findAllByPathStartingWith(String pathPrefix);
    void delete(Category category);
    List<Category> findAllByParentIsNull();
    List<Category> findAllByParentId(Long parentId);
    List<Category> findAllById(List<Long> ids);
    List<Category> findAll();
}
