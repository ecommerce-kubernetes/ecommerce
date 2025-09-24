package com.example.product_service.repository;

import com.example.product_service.entity.ProductSummary;
import com.example.product_service.repository.query.ProductSummaryQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductSummaryRepository extends JpaRepository<ProductSummary, Long>, ProductSummaryQueryRepository {

    @Query("SELECT AVG(NULLIF(ps.avgRating, 0)) FROM ProductSummary ps")
    Double findAvgRating();
}
