package com.example.product_service.product.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.ProductErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.product.application.port.ProductCategoryPort;
import com.example.product_service.product.application.port.ProductRepository;
import com.example.product_service.product.application.port.dto.ProductCategoryResult;
import com.example.product_service.product.application.service.dto.command.*;
import com.example.product_service.product.domain.Product;
import com.example.product_service.product.domain.context.CreateProductContext;
import com.example.product_service.product.domain.context.UpdateProductContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;

    private final ProductCategoryPort productCategoryPort;

    private final IdGenerator idGenerator;

    private final Clock clock;

    public Long createProduct(CreateProductCommand command) {
        ProductCategoryResult category = productCategoryPort.getCategory(command.categoryId());

        if (!category.isLeaf()) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_LEAF);
        }

        CreateProductContext context = mapToCreateProductContext(idGenerator.generate(), command);

        Product product = Product.create(context);

        return productRepository.save(product).getId();
    }

    public Long updateProduct(UpdateProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ProductCategoryResult category = productCategoryPort.getCategory(command.categoryId());

        if (!category.isLeaf()) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_LEAF);
        }

        UpdateProductContext context = mapToUpdateProductContext(command);

        product.update(context);

        return product.getId();
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

    private CreateProductContext mapToCreateProductContext(Long id, CreateProductCommand command) {
        return CreateProductContext.builder()
                .id(id)
                .categoryId(command.categoryId())
                .name(command.name())
                .description(command.description())
                .build();
    }

    private UpdateProductContext mapToUpdateProductContext(UpdateProductCommand context) {
        return UpdateProductContext.builder()
                .name(context.name())
                .categoryId(context.categoryId())
                .description(context.description())
                .build();
    }
}
