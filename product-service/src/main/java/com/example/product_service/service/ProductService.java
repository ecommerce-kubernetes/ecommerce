package com.example.product_service.service;

import com.example.product_service.common.MessagePath;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductsRepository productsRepository;
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        Categories category = findCategoryByIdOrThrow(request.getCategoryId());
        Map<Long, OptionTypes> productOptionTypeMap = getProductOptionTypeMap(request.getProductOptionTypes());
        List<ProductVariantRequest> productVariants = request.getProductVariants();

        Set<Long> requiredOptionTypeIds = new HashSet<>(productOptionTypeMap.keySet());
        for (ProductVariantRequest productVariant : productVariants) {
            validateVariantOptionTypeSetExact(requiredOptionTypeIds, productVariant.getVariantOption());
            validateVariantOptionValueExistAndBelong(productOptionTypeMap.values().stream().toList(), productVariant.getVariantOption());
            validateConflictSku(productVariant.getSku());
        }

        Products product = new Products(request.getName(), request.getDescription(), category);
        for(ImageRequest imageRequest : request.getImages()){
            ProductImages productImages = new ProductImages(imageRequest.getUrl(), imageRequest.getSortOrder());
            product.addImage(productImages);
        }
        for (Map.Entry<Long, OptionTypes> entry : productOptionTypeMap.entrySet()) {
            ProductOptionTypeRequest productOptionTypeRequest = request.getProductOptionTypes().stream()
                    .filter(otr -> Objects.equals(entry.getKey(), otr.getOptionTypeId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND)));
            ProductOptionTypes productOptionTypes = new ProductOptionTypes(entry.getValue(), productOptionTypeRequest.getPriority(), true);
            product.addOptionType(productOptionTypes);
        }
        for(ProductVariantRequest variant : productVariants){
            ProductVariants productVariant = new ProductVariants(variant.getSku(), variant.getPrice(), variant.getStockQuantity(), variant.getDiscountRate());

            for(VariantOptionValueRequest variantRequest :variant.getVariantOption()){
                OptionValues optionValue = findOptionValueByIdOrThrow(variantRequest.getOptionValueId());
                ProductVariantOptions productVariantOptions = new ProductVariantOptions(optionValue);
                productVariant.addProductVariantOption(productVariantOptions);
            }

            product.addVariant(productVariant);
        }

        Products save = productsRepository.save(product);
        return new ProductResponse(save);
    }

    private Map<Long, OptionTypes> getProductOptionTypeMap(List<ProductOptionTypeRequest> requestOptionTypes){
        List<Long> reqTypeIds = requestOptionTypes.stream().map(ProductOptionTypeRequest::getOptionTypeId).toList();
        List<OptionTypes> findTypes = optionTypeRepository.findByIdIn(reqTypeIds);

        if(reqTypeIds.size() != findTypes.size()){
            throw new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND));
        }

        return findTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, Function.identity()));
    }

    private void validateConflictSku(String sku){
        boolean isConflict = productVariantsRepository.existsBySku(sku);
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(PRODUCT_VARIANT_CONFLICT));
        }
    }

    private void validateVariantOptionTypeSetExact(Set<Long> requiredOptionTypeIds, List<VariantOptionValueRequest> opts){
        Set<Long> variantOptionTypeIds =
                opts.stream().map(VariantOptionValueRequest::getOptionTypeId).collect(Collectors.toSet());
        if(!variantOptionTypeIds.equals(requiredOptionTypeIds)){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }
    }

    private void validateVariantOptionValueExistAndBelong(List<OptionTypes> optionTypes, List<VariantOptionValueRequest> opts){
        for(VariantOptionValueRequest valueRequest : opts){
            OptionTypes findOptionType = optionTypes.stream().filter(ot -> Objects.equals(ot.getId(), valueRequest.getOptionTypeId()))
                    .findFirst().orElseThrow(() -> new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)));

            boolean isMatch = findOptionType.getOptionValues().stream().anyMatch(ov -> Objects.equals(ov.getId(), valueRequest.getOptionValueId()));
            if(!isMatch){
                throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
            }
        }
    }

    private Categories findCategoryByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }

    private OptionTypes findOptionTypeByIdOrThrow(Long optionTypeId){
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND)));
    }

    private OptionValues findOptionValueByIdOrThrow(Long optionValueId){
        return optionValueRepository.findById(optionValueId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_VALUE_NOT_FOUND)));
    }

    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request) {
        return null;
    }

    public void deleteProductById(Long productId) {

    }


    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return null;
    }


    public ProductResponse getProductById(Long productId) {
        return null;
    }


    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        return null;
    }


    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return null;
    }



    public List<ImageResponse> addImages(Long productId, AddImageRequest request) {
        return List.of();
    }


    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request) {
        return null;
    }

}
