package com.example.product_service.api.product.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.dto.PageDto;
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
import com.example.product_service.api.product.service.dto.command.ProductCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.result.*;
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

    public ProductResult.Create createProduct(ProductCommand.Create command) {
        Category category = findCategoryByIdOrThrow(command.categoryId());
        Product product = Product.create(command.name(), command.description(), category);
        Product savedProduct = productRepository.save(product);
        return ProductResult.Create.from(savedProduct);
    }

    public ProductResult.OptionRegister defineOptions(ProductCommand.OptionRegister command) {
        Product product = findProductByIdOrThrow(command.productId());
        List<OptionType> optionTypes = findOptionTypes(command.optionTypeIds());
        product.updateOptions(optionTypes);
        return ProductResult.OptionRegister.from(product);
    }

    public ProductResult.AddVariant createVariants(ProductCommand.AddVariant command) {
        // 동일한 옵션 조합의 상품 변형이 존재하는지 검증
        validateRequestUniqueCombination(command.variants());
        Product product = findProductByIdOrThrow(command.productId());
        Map<Long, OptionValue> variantOptionMap = findAndMapOptionValues(command.variants());

        //새로 추가되는 상품 변형
        List<ProductVariant> newlyCreatedVariants = new ArrayList<>();
        for (ProductCommand.VariantDetail variantReq : command.variants()) {
            // 요청 variant 의 optionValue를 찾음
            List<OptionValue> optionValues = mapToOptionValues(variantReq.optionValueIds(), variantOptionMap);
            // ProductVariant 생성
            String sku = skuGenerator.generate(product, optionValues);
            ProductVariant variant = ProductVariant.create(sku, variantReq.originalPrice(), variantReq.stockQuantity(), variantReq.discountRate());
            variant.addProductVariantOptions(optionValues);
            product.addVariant(variant);
            newlyCreatedVariants.add(variant);
        }
        productRepository.flush();
        return ProductResult.AddVariant.of(product.getId(), newlyCreatedVariants);
    }

    public ProductImageCreateResult updateImages(Long productId, List<String> images) {
        Product product = findProductByIdOrThrow(productId);
        product.replaceImages(images);
        return ProductImageCreateResult.of(product.getId(), product.getImages());
    }

    public ProductDescriptionImageResult updateDescriptionImages(Long productId, List<String> images) {
        Product product = findProductByIdOrThrow(productId);
        product.replaceDescriptionImage(images);
        return ProductDescriptionImageResult.of(productId, product.getDescriptionImages());
    }

    public ProductStatusResult publish(Long productId) {
        Product product = findProductByIdOrThrow(productId);
        product.publish();
        return ProductStatusResult.publish(product);
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
        Product product = findProductByIdOrThrow(command.getProductId());
        Category category = findCategoryByIdOrThrow(command.getCategoryId());
        product.updateProductInfo(command.getName(), command.getDescription(), category);
        return ProductUpdateResponse.from(product);
    }

    public void deleteProduct(Long productId) {
        Product product = findProductByIdOrThrow(productId);
        product.deleted();
    }

    public ProductStatusResult closedProduct(Long productId) {
        Product product = findProductByIdOrThrow(productId);
        product.closed();
        return ProductStatusResult.closed(product);
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
    private void validateRequestUniqueCombination(List<ProductCommand.VariantDetail> variants){
        long distinctRequestCount = variants.stream()
                .map(v -> new HashSet<>(v.optionValueIds()))
                .distinct()
                .count();
        if (distinctRequestCount != variants.size()) {
            throw new BusinessException(ProductErrorCode.VARIANT_DUPLICATED_IN_REQUEST);
        }
    }

    private Map<Long, OptionValue> findAndMapOptionValues(List<ProductCommand.VariantDetail> variants) {
        List<Long> optionValueIds = variants.stream().flatMap(v -> v.optionValueIds().stream()).toList();
        List<OptionValue> optionValues = optionValueRepository.findByIdIn(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValue::getId, Function.identity()));
    }
}
