package com.example.product_service.service.util.validator;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Component
@RequiredArgsConstructor
public class RequestValidator {
    private final MessageSourceUtil ms;

    public void validateProductRequest(ProductRequest request){
        List<ProductOptionTypeRequest> productOptionTypes = request.getProductOptionTypes();
        List<ProductVariantRequest> productVariants = request.getProductVariants();

        Set<Long> optionTypeIdSet = validateProductOptionTypeRequest(productOptionTypes);
        validateProductVariantRequest(optionTypeIdSet, productVariants);
    }

    public void validateVariantRequest(ProductVariantRequest request, Products product){
        Set<Long> requiredOptionTypeIds = product.getProductOptionTypes()
                .stream().map(pot -> pot.getOptionType().getId()).collect(Collectors.toSet());
        validateVariantOptionValueCardinality(requiredOptionTypeIds, request);
    }

    private Set<Long> validateProductOptionTypeRequest(List<ProductOptionTypeRequest> optionTypes){
        Set<Long> uniqueOptionTypeIds = new HashSet<>();
        Set<Integer> uniquePriorities = new HashSet<>();

        for(ProductOptionTypeRequest request : optionTypes){
            if(!uniqueOptionTypeIds.add(request.getOptionTypeId())){
                throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
            }
            if(!uniquePriorities.add(request.getPriority())){
                throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
            }
        }

        return Collections.unmodifiableSet(uniqueOptionTypeIds);
    }

    private void validateProductVariantRequest(Set<Long> requiredOptionTypeIds, List<ProductVariantRequest> variantRequests){
        validateDuplicatedSku(variantRequests);
        for(ProductVariantRequest variantRequest : variantRequests){
            validateVariantOptionValueCardinality(requiredOptionTypeIds, variantRequest);
        }
        validateDuplicateVariantCombination(variantRequests);
    }

    private void validateVariantOptionValueCardinality(Set<Long> requiredOptionTypeIds, ProductVariantRequest variantRequest) {
        List<VariantOptionValueRequest> variantOption = variantRequest.getVariantOption();

        if(variantOption.size() != requiredOptionTypeIds.size()){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }

        List<Long> optionTypeIdList = variantOption.stream()
                .map(VariantOptionValueRequest::getOptionTypeId).toList();

        if(optionTypeIdList.size() != new HashSet<>(optionTypeIdList).size()){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }

        Set<Long> optionTypeIdSet = new HashSet<>(optionTypeIdList);
        if(!optionTypeIdSet.equals(requiredOptionTypeIds)){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }
    }

    private void validateDuplicatedSku(List<ProductVariantRequest> variantRequests){
        Set<String> uniqueSku = new HashSet<>();
        for(ProductVariantRequest variantRequest : variantRequests){
            if(!uniqueSku.add(variantRequest.getSku())){
                throw new BadRequestException(ms.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
            }
        }
    }

    private void validateDuplicateVariantCombination(List<ProductVariantRequest> variantRequests){
        Set<Set<Long>> seenCombinations = new HashSet<>();
        for (ProductVariantRequest variant : variantRequests) {
            Set<Long> combination = variant.getVariantOption().stream()
                    .map(VariantOptionValueRequest::getOptionValueId)
                    .collect(Collectors.toSet());

            if (!seenCombinations.add(combination)) {
                throw new BadRequestException(ms.getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
            }
        }
    }
}
