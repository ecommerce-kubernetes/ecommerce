package com.example.product_service.api.product.domain.repository;

import com.example.product_service.api.product.domain.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("""
            select distinct pv
            from ProductVariant pv
            join fetch pv.product p
            join fetch pv.productVariantOptions pvo
            join fetch pvo.optionValue ov
            join fetch ov.optionType ot
            where pv.id = :id
            """)
    Optional<ProductVariant> findByIdWithProductAndOption(@Param("id") Long id);

    @Query("""
            select distinct pv
            from ProductVariant pv
            join fetch pv.product p
            join fetch pv.productVariantOptions pvo
            join fetch pvo.optionValue ov
            join fetch ov.optionType ot
            where pv.id in :ids
            """)
    List<ProductVariant> findByIdInWithProductAndOption(@Param("ids") List<Long> ids);

    @Query("select pv from ProductVariant pv where pv.id in :ids")
    List<ProductVariant> findByIdIn(@Param("ids") List<Long> ids);
}
