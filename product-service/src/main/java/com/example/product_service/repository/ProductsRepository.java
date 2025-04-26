package com.example.product_service.repository;

import com.example.product_service.entity.Products;
import com.example.product_service.repository.query.ProductQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Products, Long>, ProductQueryRepository {
    List<Products> findAllByIdIn(List<Long> productIds);

    @Query("SELECT p FROM Products p JOIN FETCH p.images WHERE p.id =:id")
    Optional<Products> findByIdWithProductImages(@Param("id") Long id);


}
