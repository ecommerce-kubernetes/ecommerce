package com.example.product_service.repository;

import com.example.product_service.entity.ProductOptionTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionTypesRepository extends JpaRepository<ProductOptionTypes, Long> {

    @Query("SELECT pot FROM ProductOptionTypes pot JOIN FETCH pot.optionType WHERE pot.product.id = :productId")
    List<ProductOptionTypes> findWithOptionTypeByProductId(@Param("productId") Long productId);
}
