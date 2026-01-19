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
import com.example.product_service.api.option.domain.repository.OptionValueRepository;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.api.common.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final SkuGenerator skuGenerator;

    public ProductCreateResponse createProduct(ProductCreateCommand command) {
        Category category = findCategoryByIdOrThrow(command.getCategoryId());
        Product product = Product.create(command.getName(), command.getDescription(), category);
        Product savedProduct = productRepository.save(product);
        return ProductCreateResponse.from(savedProduct);
    }

    public ProductOptionResponse defineOptions(Long productId, List<Long> optionTypeIds) {
        Product product = findProductByIdOrThrow(productId);
        List<OptionType> optionTypes = findOptionTypes(optionTypeIds);
        product.updateOptions(optionTypes);
        return ProductOptionResponse.of(product.getId(), product.getOptions());
    }

    public VariantCreateResponse createVariants(ProductVariantsCreateCommand command) {
        // 동일한 옵션 조합의 상품 변형이 존재하는지 검증
        validateRequestUniqueCombination(command.getVariants());
        Product product = findProductByIdOrThrow(command.getProductId());
        Map<Long, OptionValue> variantOptionMap = findAndMapOptionValues(command.getVariants());

        //새로 추가되는 상품 변형
        List<ProductVariant> newlyCreatedVariants = new ArrayList<>();
        for (ProductVariantsCreateCommand.VariantDetail variantReq : command.getVariants()) {
            // 요청 variant 의 optionValue를 찾음
            List<OptionValue> optionValues = mapToOptionValues(variantReq.getOptionValueIds(), variantOptionMap);
            // ProductVariant 생성
            String sku = skuGenerator.generate(product, optionValues);
            ProductVariant variant = ProductVariant.create(sku, variantReq.getOriginalPrice(), variantReq.getStockQuantity(), variantReq.getDiscountRate());
            variant.addProductVariantOptions(optionValues);
            product.addVariant(variant);
            newlyCreatedVariants.add(variant);
        }
        productRepository.flush();
        return VariantCreateResponse.of(product.getId(), newlyCreatedVariants);
    }

    public ProductImageCreateResponse updateImages(Long productId, List<String> images) {
        Product product = findProductByIdOrThrow(productId);
        product.replaceImages(images);
        return ProductImageCreateResponse.of(product.getId(), product.getImages());
    }

    public ProductStatusResponse publish(Long productId) {
        Product product = findProductByIdOrThrow(productId);
        product.publish();
        return ProductStatusResponse.publish(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long productId){
        Product product = findProductByIdOrThrow(productId);
        return ProductDetailResponse.from(product);
    }

    @Transactional(readOnly = true)
    public PageDto<ProductSummaryResponse> getProducts(ProductSearchCondition condition){
        Page<Product> products = productRepository.findProductsByCondition(condition);
        return PageDto.of(products, ProductSummaryResponse::from);
    }

    public ProductUpdateResponse updateProduct(ProductUpdateCommand command) {
        return null;
    }

    public void deleteProduct(Long productId) {

    }

    public ProductStatusResponse closedProduct(Long productId) {
        return null;
    }

    private List<OptionType> findOptionTypes(List<Long> optionTypeIds) {
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

    private List<OptionValue> mapToOptionValues(List<Long> ids, Map<Long, OptionValue> map) {
        return ids.stream()
                .map(id -> {
                    OptionValue value = map.get(id);
                    if (value == null)
                        throw new BusinessException(OptionErrorCode.OPTION_NOT_FOUND);
                    return value;
                }).toList();
    }

    // 동일한 옵션 조합을 가진 상품 변형이 있는지 검증
    private void validateRequestUniqueCombination(List<ProductVariantsCreateCommand.VariantDetail> variants){
        long distinctRequestCount = variants.stream()
                .map(v -> new HashSet<>(v.getOptionValueIds()))
                .distinct()
                .count();
        if (distinctRequestCount != variants.size()) {
            throw new BusinessException(ProductErrorCode.VARIANT_DUPLICATED_IN_REQUEST);
        }
    }

    private Map<Long, OptionValue> findAndMapOptionValues(List<ProductVariantsCreateCommand.VariantDetail> variants) {
        List<Long> optionValueIds = variants.stream().flatMap(v -> v.getOptionValueIds().stream()).toList();
        List<OptionValue> optionValues = optionValueRepository.findByIdIn(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValue::getId, Function.identity()));
    }
}
