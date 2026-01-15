package com.example.product_service.repository;

import com.example.product_service.api.product.domain.model.ProductOptionSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionTypesRepository extends JpaRepository<ProductOptionSpec, Long> {

    @Query("SELECT pot FROM ProductOptionSpec pot JOIN FETCH pot.optionType WHERE pot.product.id = :productId")
    List<ProductOptionSpec> findWithOptionTypeByProductId(@Param("productId") Long productId);
}
