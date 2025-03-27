package com.example.product_service.repository;

import com.example.product_service.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductsRepository extends JpaRepository<Products, Long> {

    @Query("SELECT p FROM Products p")
    Page<Products> findAllProducts(Pageable pageable);
}
