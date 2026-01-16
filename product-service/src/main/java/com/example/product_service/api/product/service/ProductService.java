package com.example.product_service.api.product.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductOptionSpec;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.api.option.domain.repository.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
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
    private final OptionValueRepository optionValueRepository;

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

    public VariantCreateResponse createVariants(ProductVariantsCreateCommand command) {
        validateDuplicateVariantRequest(command.getVariants());
        Product product = findProductByIdOrThrow(command.getProductId());
        validateCreatableVariant(product, command.getVariants());
        Map<Long, OptionValue> variantOptionMap = getRequestVariantOptionMap(command.getVariants());
        mappingVariants(product, command.getVariants(), variantOptionMap);
        // 저장
        productRepository.saveAndFlush(product);
        return VariantCreateResponse.of(product.getId(), product.getVariants());
    }

    private void mappingVariants(Product product, List<ProductVariantsCreateCommand.VariantDetail> variants, Map<Long, OptionValue> requestVariantOptionMap) {
        for (ProductVariantsCreateCommand.VariantDetail variantReq : variants) {
            List<OptionValue> currentVariantOptionValues = new ArrayList<>();
            // 요청 변형 옵션중 존재하지 않는 옵션이 있는지 검증
            validateNotFoundOptionValue(variantReq.getOptionValueIds(), requestVariantOptionMap, currentVariantOptionValues);
            //요청 변형 옵션 중 상품 옵션에 해당하지 않는 옵션이 있는지 검증
            List<OptionValue> sortedOptionValues = new ArrayList<>();
            validateOptionSpec(product.getOptionSpecs(), currentVariantOptionValues, sortedOptionValues);
            // 상품에 동일한 상품 변형이 존재하는지 검증
            validateHasDuplicateVariant(product.getVariants(), variantReq);
            // 상품 변형 생성
            linkProductVariant(product, variantReq, sortedOptionValues);
        }
    }

    private void validateHasDuplicateVariant(List<ProductVariant> existVariants, ProductVariantsCreateCommand.VariantDetail variantReq) {
        for (ProductVariant variant : existVariants) {
            List<Long> existIds = variant.getProductVariantOptions().stream().map(option -> option.getOptionValue().getId()).toList();
            boolean isDuplicate = new HashSet<>(existIds).equals(new HashSet<>(variantReq.getOptionValueIds()));
            if (isDuplicate) {
                throw new BusinessException(ProductErrorCode.PRODUCT_HAS_DUPLICATE_VARIANT);
            }
        }
    }

    private void validateNotFoundOptionValue(List<Long> optionValueIds, Map<Long, OptionValue> variantOptionMap, List<OptionValue> optionValues) {
        for(Long optionValueId : optionValueIds) {
            OptionValue optionValue = variantOptionMap.get(optionValueId);
            if (optionValue == null) {
                throw new BusinessException(OptionErrorCode.OPTION_NOT_FOUND);
            }
            optionValues.add(optionValue);
        }
    }

    private void validateOptionSpec(List<ProductOptionSpec> optionSpecs, List<OptionValue> currentVariantOptionValues, List<OptionValue> sortedOptionValues) {
        for(ProductOptionSpec spec : optionSpecs) {
            OptionValue matchedValue = currentVariantOptionValues.stream()
                    .filter(val -> val.getOptionType().equals(spec.getOptionType()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC));

            sortedOptionValues.add(matchedValue);
        }
    }

    private void linkProductVariant(Product product, ProductVariantsCreateCommand.VariantDetail req, List<OptionValue> optionValues) {
        ProductVariant variant = ProductVariant.create("PROD", req.getOriginalPrice(), req.getStockQuantity(), req.getDiscountRate());
        variant.addProductVariantOptions(optionValues);
        product.addVariant(variant);
    }

    private void validateDuplicateVariantRequest(List<ProductVariantsCreateCommand.VariantDetail> variants){
        long distinctRequestCount = variants.stream()
                .map(v -> new HashSet<>(v.getOptionValueIds()))
                .distinct()
                .count();
        if (distinctRequestCount != variants.size()) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_VARIANT_IN_REQUEST);
        }
    }

    private void validateCreatableVariant(Product product, List<ProductVariantsCreateCommand.VariantDetail> variants) {
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        for (ProductVariantsCreateCommand.VariantDetail variantReq: variants) {
            if (variantReq.getOptionValueIds().size() != product.getOptionSpecs().size()) {
                throw new BusinessException(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC);
            }
        }
    }

    private Map<Long, OptionValue> getRequestVariantOptionMap(List<ProductVariantsCreateCommand.VariantDetail> variants) {
        List<Long> optionValueIds = variants.stream().flatMap(v -> v.getOptionValueIds().stream()).toList();
        List<OptionValue> optionValues = optionValueRepository.findByIdIn(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValue::getId, Function.identity()));
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
