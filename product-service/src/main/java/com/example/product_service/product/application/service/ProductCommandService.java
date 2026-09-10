package com.example.product_service.product.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.domain.context.*;
import com.example.product_service.product.exception.ProductErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.product.application.port.ProductCategoryPort;
import com.example.product_service.product.application.port.ProductOptionPort;
import com.example.product_service.product.application.port.ProductRepository;
import com.example.product_service.product.application.port.dto.ProductCategoryResult;
import com.example.product_service.product.application.port.dto.ProductOptionTypesResult;
import com.example.product_service.product.application.service.dto.command.*;
import com.example.product_service.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;

    private final ProductCategoryPort productCategoryPort;

    private final ProductOptionPort productOptionPort;

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
        Product product = getProductById(command.productId());

        ProductCategoryResult category = productCategoryPort.getCategory(command.categoryId());

        if (!category.isLeaf()) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_LEAF);
        }

        UpdateProductContext context = mapToUpdateProductContext(command);

        product.update(context);

        return product.getId();
    }

    public Long addProductImages(AddProductImageCommand command) {
        Product product = getProductById(command.productId());

        List<AddMainImageContext> contexts = command.images().stream()
                .map(image -> mapToMainImageContext(idGenerator.generate(), image)).toList();

        product.addMainImages(contexts);

        return product.getId();
    }

    public Long addProductDescriptionImages(AddProductDescriptionImageCommand command) {
        Product product = getProductById(command.productId());

        List<AddDetailImageContext> contexts = command.images().stream()
                .map(image -> mapToDetailImageContext(idGenerator.generate(), image)).toList();

        product.addDetailImages(contexts);

        return product.getId();
    }

    public Long registerProductOptions(RegisterProductOptionCommand command) {
        Product product = getProductById(command.productId());

        ProductOptionTypesResult optionTypes = productOptionPort.getOptionTypes(command.optionTypeIds());

        Set<Long> requestedIds = new HashSet<>(command.optionTypeIds());

        if (requestedIds.size() != optionTypes.optionTypes().size()) {
            throw new BusinessException(ProductErrorCode.OPTION_TYPE_NOT_FOUND);
        }

        List<RegisterOptionTypeContext> registerOptionTypeContexts = optionTypes.optionTypes().stream()
                .map(optionType -> mapToRegisterOptionTypeContext(idGenerator.generate(), optionType.id())).toList();

        product.registerOptionTypes(registerOptionTypeContexts);

        return product.getId();
    }

    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        product.deleted(LocalDateTime.now(clock));
    }

    public Long addProductVariants(AddProductVariantCommand command) {
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

    private RegisterOptionTypeContext mapToRegisterOptionTypeContext(Long id, Long optionTypeId) {
        return RegisterOptionTypeContext.builder()
                .id(id)
                .optionTypeId(optionTypeId)
                .build();
    }

    private AddMainImageContext mapToMainImageContext(Long id, String imagePath) {
        return AddMainImageContext.builder()
                .id(id)
                .imagePath(imagePath)
                .build();
    }

    private AddDetailImageContext mapToDetailImageContext(Long id, String imagePath) {
        return AddDetailImageContext.builder()
                .id(id)
                .imagePath(imagePath)
                .build();
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

}
