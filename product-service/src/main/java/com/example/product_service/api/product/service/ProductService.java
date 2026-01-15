package com.example.product_service.api.product.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.api.product.service.dto.command.AddVariantCommand;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.dto.response.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OptionTypeRepository optionTypeRepository;

    public ProductCreateResponse createProduct(ProductCreateCommand command) {
        Category category = findCategoryByIdOrThrow(command.getCategoryId());
        Product product = Product.create(command.getName(), command.getDescription(), category);
        Product savedProduct = productRepository.save(product);
        return ProductCreateResponse.from(savedProduct);
    }

    public ProductOptionSpecResponse registerOptionSpec(Long productId, List<Long> optionTypeIds) {
        Product product = findProductByIdOrThrow(productId);
        validateRegisterOptionSpec(product);
        List<OptionType> sortedOptionTypes = findSortedOptionTypes(optionTypeIds);
        product.updateOptionSpecs(sortedOptionTypes);
        productRepository.saveAndFlush(product);
        return ProductOptionSpecResponse.of(product.getId(), product.getOptionSpecs());
    }

    public VariantCreateResponse addVariants(AddVariantCommand command) {
        return null;
    }

    public ProductImageCreateResponse addImages(Long productId, List<String> images) {
        return null;
    }

    public ProductStatusResponse publish(Long productId) {
        return null;
    }

    public PageDto<ProductSummaryResponse> getProducts(ProductSearchCondition condition){
        return null;
    }

    public ProductDetailResponse getProduct(Long productId){
        return null;
    }

    public ProductUpdateResponse updateProduct(ProductUpdateCommand command) {
        return null;
    }

    public void deleteProduct(Long productId) {

    }

    public ProductStatusResponse closedProduct(Long productId) {
        return null;
    }

    private void validateRegisterOptionSpec(Product product) {
        if (product.getStatus().equals(ProductStatus.ON_SALE)){
            throw new BusinessException(ProductErrorCode.CANNOT_MODIFY_ON_SALE);
        }
        if (!product.getVariants().isEmpty()) {
            throw new BusinessException(ProductErrorCode.HAS_VARIANTS);
        }
    }

    private List<OptionType> findSortedOptionTypes(List<Long> optionTypeIds) {
        List<OptionType> optionTypes = optionTypeRepository.findByIdIn(optionTypeIds);
        if (optionTypes.size() != optionTypeIds.size()) {
            throw new BusinessException(OptionErrorCode.OPTION_NOT_FOUND);
        }

        Map<Long, OptionType> typeMap = optionTypes.stream().collect(Collectors.toMap(OptionType::getId, Function.identity()));
        return optionTypeIds.stream().map(typeMap::get).toList();
    }

    private Category findCategoryByIdOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    private Product findProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
