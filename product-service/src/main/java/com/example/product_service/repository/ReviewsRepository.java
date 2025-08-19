package com.example.product_service.repository;

import com.example.product_service.entity.Reviews;
import com.example.product_service.repository.query.ReviewsQueryRepository;
import com.example.product_service.service.dto.ReviewStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewsRepository extends JpaRepository<Reviews, Long>, ReviewsQueryRepository {
    @Query("SELECT new com.example.product_service.service.dto.ReviewStats(COUNT(r),COALESCE(AVG(r.rating), 0))" +
            "FROM Reviews r WHERE r.productVariant.product.id = :productId")
    ReviewStats findReviewStatsByProductId(@Param("productId") Long productId);
}
