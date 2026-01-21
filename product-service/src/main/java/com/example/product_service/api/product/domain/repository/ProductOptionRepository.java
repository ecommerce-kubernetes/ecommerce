package com.example.product_service.api.product.domain.repository;

import com.example.product_service.api.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @Query("select count(po) > 0 from ProductOption po where po.optionType.id = :optionTypeId")
    boolean existByOptionTypeId(@Param("optionTypeId") Long optionTypeId);
}
