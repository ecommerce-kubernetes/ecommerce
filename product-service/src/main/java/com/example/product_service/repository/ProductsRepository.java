package com.example.product_service.repository;

import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.entity.Products;
import com.example.product_service.repository.query.ProductQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Products, Long>, ProductQueryRepository {
    List<Products> findAllByIdIn(List<Long> productIds);

    @Query("SELECT p FROM Products p JOIN FETCH p.images WHERE p.id =:id")
    Optional<Products> findByIdWithProductImages(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p 
            FROM Products p 
                JOIN FETCH p.images 
                LEFT JOIN FETCH p.category
            WHERE p.id = :id
            """)
    Optional<Products> findByIdWithImageAndCategory(@Param("id") Long id);


    @Query("""
            SELECT DISTINCT p
            FROM Products p
                JOIN FETCH p.images
            WHERE p.id IN :ids
            """)
    List<Products> findAllByIdInWithImages(@Param("ids") List<Long> ids);

    @Query("""
            SELECT new com.example.product_service.dto.response.CompactProductResponseDto(
                p,
                img.imageUrl
            )
            FROM Products p
            LEFT JOIN p.images img 
                ON img.sortOrder = 0
            WHERE p.id IN :ids
            """)
    List<CompactProductResponseDto> findAllWithRepresentativeImageByIds(@Param("ids") List<Long> ids);
}
