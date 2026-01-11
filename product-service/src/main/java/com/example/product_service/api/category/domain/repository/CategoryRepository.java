package com.example.product_service.api.category.domain.repository;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.query.CategoryQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryQueryDslRepository {

    List<Category> findByParentIsNull();

    @Query("select c from Category c where c.id in :categoryIds order by c.depth asc")
    List<Category> findByInOrderDepth(@Param("categoryIds") List<Long> categoryIds);

    @Query("select c from Category c where c.parent.id = :parentId")
    List<Category> findByParentId(@Param("parentId") Long parentId);
}
