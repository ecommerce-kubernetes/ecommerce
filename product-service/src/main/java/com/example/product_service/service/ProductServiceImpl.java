package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;

    @Override
    @Transactional
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {
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
        return new ProductResponseDto(save);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        productsRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        int updateStockQuantity = stockQuantityRequestDto.getUpdateStockQuantity();
        product.setStockQuantity(updateStockQuantity);

        return new ProductResponseDto(product);
    }

    @Override
    public ProductResponseDto getProductDetails(Long productId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        return new ProductResponseDto(product);
    }

    @Override
    public PageDto<ProductResponseDto> getProductList(Pageable pageable, Long categoryId, String name) {
//        Page<Products> productsPage = productsRepository.findAllProducts(pageable);

        Page<Products> productsPage = productsRepository.findAllByParameter(name, categoryId, pageable);
        List<ProductResponseDto> content = productsPage.getContent().stream().map(ProductResponseDto::new).toList();

        return new PageDto<>(
                content,
                pageable.getPageNumber(),
                productsPage.getTotalPages(),
                pageable.getPageSize(),
                productsPage.getTotalElements()
        );
    }
}
