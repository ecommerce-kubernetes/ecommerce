package com.example.product_service.product.adapter.out.persistence;

import com.example.product_service.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}
