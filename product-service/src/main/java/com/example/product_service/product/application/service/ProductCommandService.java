package com.example.product_service.product.application.service;

import com.example.product_service.product.application.service.dto.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    public Long createProduct(CreateProductCommand command) {
        return null;
    }

    public Long updateProduct(UpdateProductCommand command) {
        return null;
    }

    public void deleteProduct(Long productId) {

    }

    public Long registerProductOptions(RegisterProductOptionCommand command) {
        return null;
    }

    public Long addProductVariants(AddProductVariantCommand command) {
        return null;
    }

    public Long addProductImages(AddProductImageCommand command) {
        return null;
    }

    public Long addProductDescriptionImages(AddProductDescriptionImageCommand command) {
        return null;
    }
}
