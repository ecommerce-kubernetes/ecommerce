package com.example.product_service.service.util;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Component
@RequiredArgsConstructor
public class ProductRequestValidator {
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public void validateProductRequestStructure(Map<Long, OptionTypes> productOptionTypeMap, ProductRequest request){
        List<ProductVariantRequest> productVariants = request.getProductVariants();
        Set<Long> requiredOptionTypeIds = new HashSet<>(productOptionTypeMap.keySet());
        for (ProductVariantRequest productVariant : productVariants) {
            validateVariantOptionTypeSetExact(requiredOptionTypeIds, productVariant.getVariantOption());
            validateVariantOptionValueExistAndBelong(productOptionTypeMap, productVariant.getVariantOption());
            validateConflictSku(productVariant.getSku());
        }
    }

    private void validateVariantOptionTypeSetExact(Set<Long> requiredOptionTypeIds, List<VariantOptionValueRequest> opts){
        Set<Long> variantOptionTypeIds =
                opts.stream().map(VariantOptionValueRequest::getOptionTypeId).collect(Collectors.toSet());
        if(!variantOptionTypeIds.equals(requiredOptionTypeIds)){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }
    }

    private void validateVariantOptionValueExistAndBelong(Map<Long, OptionTypes> optionTypesMap, List<VariantOptionValueRequest> opts) {
        for (VariantOptionValueRequest valueRequest : opts) {
            OptionTypes findOptionType = optionTypesMap.get(valueRequest.getOptionTypeId());
            boolean isMatch = findOptionType.getOptionValues().stream().anyMatch(ov -> Objects.equals(ov.getId(), valueRequest.getOptionValueId()));
            if (!isMatch) {
                throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
            }
        }
    }
    private void validateConflictSku(String sku){
        boolean isConflict = productVariantsRepository.existsBySku(sku);
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(PRODUCT_VARIANT_CONFLICT));
        }
    }
}
