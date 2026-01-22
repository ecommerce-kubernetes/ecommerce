package com.example.product_service.api.product.domain.repository;

import com.example.product_service.api.product.domain.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantOptionRepository extends JpaRepository<ProductVariant, Long> {
    @Query("select count(pvo) > 0 from ProductVariantOption pvo where pvo.optionValue.id = :optionValueId")
    boolean existByOptionValueId(@Param("optionValueId") Long optionValueId);
}
