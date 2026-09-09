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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;

    private final ProductCategoryPort productCategoryPort;

    private final IdGenerator idGenerator;

    public Long createProduct(CreateProductCommand command) {
        ProductCategoryResult category = productCategoryPort.getCategory(command.categoryId());

        if (!category.isLeaf()) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_LEAF);
        }

        CreateProductContext context = mapToCreateCategoryContext(idGenerator.generate(), command);

        Product product = Product.create(context);

        return productRepository.save(product).getId();
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

    private CreateProductContext mapToCreateCategoryContext(Long id, CreateProductCommand command) {
        return CreateProductContext.builder()
                .id(id)
                .categoryId(command.categoryId())
                .name(command.name())
                .description(command.description())
                .build();
    }

}
