package com.example.product_service.repository;

import com.example.product_service.entity.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewsRepository extends JpaRepository<Reviews, Long> {

    @Query("SELECT r FROM Reviews r WHERE r.product.id = :productId")
    List<Reviews> findByProductId(@Param("productId") Long productId);
}
