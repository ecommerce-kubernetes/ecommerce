package com.example.product_service.repository;

import com.example.product_service.entity.ProductOptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionTypesRepository extends JpaRepository<ProductOptionType, Long> {

    @Query("SELECT pot FROM ProductOptionType pot JOIN FETCH pot.optionType WHERE pot.product.id = :productId")
    List<ProductOptionType> findWithOptionTypeByProductId(@Param("productId") Long productId);
}
