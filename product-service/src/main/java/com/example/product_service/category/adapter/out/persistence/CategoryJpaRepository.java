package com.example.product_service.category.adapter.out.persistence;

import com.example.product_service.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
    boolean existsByParentIdAndName(Long parentId, String name);
    boolean existsByParentIdAndNameAndIdNot(Long parentId, String name, Long id);
    List<Category> findAllByPathStartingWith(String pathPrefix);
    List<Category> findAllByParentIsNull();
    List<Category> findAllByParentId(Long parentId);
}
