package com.example.product_service.repository;

import com.example.product_service.entity.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {

    @Query("SELECT c FROM Categories c WHERE c.parent.id = :parentId")
    List<Categories> findChildById(@Param("parentId") Long parentId);
    Page<Categories> findByParentIsNull(Pageable pageable);

    @EntityGraph(attributePaths = "parent")
    @Query("SELECT c FROM Categories c WHERE c.id = :id")
    Optional<Categories> findByIdWithParent(@Param("id") Long id);
}
