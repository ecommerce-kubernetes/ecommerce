package com.example.product_service.service.util.validator;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
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
        Set<Long> optionTypeIdSet = extractAndValidateOptionTypeIds(request.getProductOptionTypes());
        validateProductVariantRequest(optionTypeIdSet, request.getProductVariants());
    }

    public void validateVariantRequest(ProductVariantRequest request){
        List<Long> optionTypeId = request.getVariantOption().stream().map(VariantOptionValueRequest::getOptionTypeId).toList();
        ensureUniqueOptionTypeIds(optionTypeId, PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION);
    }

    private Set<Long> extractAndValidateOptionTypeIds(List<ProductOptionTypeRequest> optionTypes){
        List<Long> optionTypeIds = optionTypes.stream().map(ProductOptionTypeRequest::getOptionTypeId).toList();
        ensureUniqueOptionTypeIds(optionTypeIds, PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST);

        Set<Integer> uniquePriorities = new HashSet<>();
        for(ProductOptionTypeRequest request : optionTypes){
            if(!uniquePriorities.add(request.getPriority())){
                throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
            }
        }
        return Set.copyOf(optionTypeIds);
    }

    private void validateProductVariantRequest(Set<Long> requiredOptionTypeIds, List<ProductVariantRequest> variantRequests){
        ensureUniqueSkus(variantRequests);
        for(ProductVariantRequest variantRequest : variantRequests){
            validateVariantOptionValueCardinality(requiredOptionTypeIds, variantRequest);
        }
        ensureUniqueVariantCombinations(variantRequests);
    }

    private void validateVariantOptionValueCardinality(Set<Long> requiredOptionTypeIds, ProductVariantRequest variantRequest) {
        ensureOptionTypeCountMatches(requiredOptionTypeIds, variantRequest.getVariantOption());
        ensureOptionTypeIdsMatchRequired(requiredOptionTypeIds, variantRequest.getVariantOption());
    }

    private void ensureUniqueSkus(List<ProductVariantRequest> variantRequests){
        Set<String> uniqueSku = new HashSet<>();
        for(ProductVariantRequest variantRequest : variantRequests){
            if(!uniqueSku.add(variantRequest.getSku())){
                throw new BadRequestException(ms.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
            }
        }
    }

    private void ensureUniqueVariantCombinations(List<ProductVariantRequest> variantRequests){
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

    /**
     상품 옵션과 상품 변형 옵션의 개수가 같은지 검증
     상품 옵션 { 1L: 색상, 2L: 용량}
     검증 대상 : {1L}, {2L}, {1L,2L,3L}
    */
    private void ensureOptionTypeCountMatches(Set<Long> requiredOptionTypeIds,
                                              List<VariantOptionValueRequest> requests){
        if(requiredOptionTypeIds.size() != requests.size()){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));        }
    }

    /**
     * 중복 요청 optionTypeId 를 검증
     * 검증 대상 {1L, 1L}, {2L, 2L}
     */
    private void ensureUniqueOptionTypeIds(List<Long> optionTypeIds, String messageKey){
        Set<Long> unique = new HashSet<>();
        for (Long id : optionTypeIds) {
            if(!unique.add(id)){
                throw new BadRequestException(ms.getMessage(messageKey));
            }
        }
    }

    /**
     상품 옵션과 상품 변형 옵션의 타입 Id가 일치하는지 검증
     상품 옵션 { 1L: 색상, 2L: 용량}
     검증 대상 : {1L,3L}, {3L,2L}, {5L,6L}
     */
    private void ensureOptionTypeIdsMatchRequired(Set<Long> requiredOptionTypeIds,
                                                  List<VariantOptionValueRequest> requests){
        Set<Long> uniqueOptionTypeId =
                requests.stream().map(VariantOptionValueRequest::getOptionTypeId).collect(Collectors.toSet());

        if(!uniqueOptionTypeId.equals(requiredOptionTypeIds)){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        }
    }
}
