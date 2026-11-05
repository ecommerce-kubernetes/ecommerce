package com.example.product_service.product.adapter.out;

import com.example.product_service.product.adapter.out.persistence.ProductJpaRepository;
import com.example.product_service.product.application.port.ProductRepository;
import com.example.product_service.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }
}
