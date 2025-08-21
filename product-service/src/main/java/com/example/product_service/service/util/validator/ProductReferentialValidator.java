package com.example.product_service.service.util.validator;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Component
@RequiredArgsConstructor
public class ProductReferentialValidator {
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public ProductCreationData validAndFetch(ProductRequest request){
        validateDuplicateSkus(request);
        Categories categories = findByIdOrThrow(request.getCategoryId());
        List<OptionTypes> optionTypes = findOptionTypes(request);
        Map<Long, OptionTypes> optionTypeById = optionTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, Function.identity()));

        Map<Long, Set<Long>> optionTypeToValueIds = optionTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, ot -> ot.getOptionValues().stream()
                        .map(OptionValues::getId).collect(Collectors.toSet())));

        validateOptionValueCardinality(request.getProductVariants(), optionTypeToValueIds);

        Map<Long, OptionValues> optionValueById = optionTypes.stream()
                .flatMap(ot -> ot.getOptionValues().stream())
                .collect(Collectors.toMap(OptionValues::getId, Function.identity()));
        return new ProductCreationData(categories, optionTypeById, optionValueById);
    }

    public ProductVariantCreationData validateProductVariant(ProductVariantRequest request, Products product){
        validateDuplicateSku(request.getSku());
        List<OptionTypes> optionTypes = product.getProductOptionTypes().stream().map(ProductOptionTypes::getOptionType).toList();
        Map<Long, Set<Long>> optionTypeToValueId = optionTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, ot->ot.getOptionValues().stream()
                        .map(OptionValues::getId).collect(Collectors.toSet())));

        validateOptionValueCardinality(Collections.singletonList(request), optionTypeToValueId);
        validateDuplicateVariantCombination(request, product);

        Map<Long, OptionValues> optionValueById = optionTypes.stream()
                .flatMap(ot -> ot.getOptionValues().stream())
                .collect(Collectors.toMap(OptionValues::getId, Function.identity()));

        return new ProductVariantCreationData(optionValueById);
    }

    private void validateDuplicateVariantCombination(ProductVariantRequest request, Products product){
        List<Set<Long>> productVariantOption = product.getProductVariants().stream().map(pv -> pv.getProductVariantOptions().stream()
                .map(pvo -> pvo.getOptionValue().getId())
                .collect(Collectors.toSet())).toList();

        Set<Long> optionValue = request.getVariantOption().stream()
                .map(VariantOptionValueRequest::getOptionValueId).collect(Collectors.toSet());
        boolean existsExact = productVariantOption.stream()
                .anyMatch(existing -> existing.equals(optionValue));

        if (existsExact) {
            throw new BadRequestException(ms.getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
        }
    }

    private void validateOptionValueCardinality(List<ProductVariantRequest> variantRequests,
                                                Map<Long, Set<Long>> optionTypeToValueIds){
        for(ProductVariantRequest variantRequest : variantRequests){
            for(VariantOptionValueRequest v : variantRequest.getVariantOption()){
                Set<Long> allowedValueIds = optionTypeToValueIds.get(v.getOptionTypeId());
                if(!allowedValueIds.contains(v.getOptionValueId())){
                    throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
                }
            }
        }
    }

    private Categories findByIdOrThrow(Long categoryId){
        Categories categories = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));

        if(!categories.isLeaf()){
            throw new BadRequestException(ms.getMessage(PRODUCT_CATEGORY_BAD_REQUEST));
        }
        return categories;
    }

    private void validateDuplicateSkus(ProductRequest request){
        List<String> skus = request.getProductVariants().stream().map(ProductVariantRequest::getSku).toList();
        ensureSkusDoNotExists(skus);
    }

    private void validateDuplicateSku(String sku){
        ensureSkuDoNotExists(sku);
    }

    private void ensureSkusDoNotExists(Collection<String> skus){
        boolean isConflict = productVariantsRepository.existsBySkuIn(skus);
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
        }
    }

    private void ensureSkuDoNotExists(String sku){
        boolean isConflict = productVariantsRepository.existsBySku(sku);
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
        }
    }

    private List<OptionTypes> findOptionTypes(ProductRequest request){
        List<Long> optionTypeIds = request.getProductOptionTypes()
                .stream().map(ProductOptionTypeRequest::getOptionTypeId).toList();
        return findOptionTypeByIdInOrThrow(optionTypeIds);
    }

    private List<OptionTypes> findOptionTypeByIdInOrThrow(List<Long> optionTypeIds){
        List<OptionTypes> result = optionTypeRepository.findByIdIn(optionTypeIds);
        if(optionTypeIds.size() != result.size()){
            throw new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND));
        }
        return result;
    }
}
