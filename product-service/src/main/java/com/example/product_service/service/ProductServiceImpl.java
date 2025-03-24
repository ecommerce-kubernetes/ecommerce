package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        Long categoryId = productRequestDto.getCategoryId();
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));
        Products products = new Products(
                productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getPrice(),
                productRequestDto.getStockQuantity(),
                category
        );
        Products save = productsRepository.save(products);
        return new ProductResponseDto(
                save.getId(),
                save.getName(),
                save.getDescription(),
                save.getPrice(),
                save.getStockQuantity(),
                save.getCategory().getId()
        );
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        productsRepository.delete(product);
    }
}
