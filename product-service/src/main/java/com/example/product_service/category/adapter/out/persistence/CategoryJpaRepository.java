package com.example.product_service.category.adapter.out.persistence;

import com.example.product_service.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}
