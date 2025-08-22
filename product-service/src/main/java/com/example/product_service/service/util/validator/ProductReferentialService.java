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
import com.example.product_service.repository.OptionValueRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductReferentialService {
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public ProductCreationData validAndFetch(ProductRequest request){
        //SKU 중복확인
        validateDuplicateSkus(request);
        //카테고리 조회 -> 없으면 예외 던짐
        Categories categories = findByIdOrThrow(request.getCategoryId());
        List<OptionTypes> optionTypes = findOptionTypes(request);
        List<List<VariantOptionValueRequest>> variantOptionValues = request.getProductVariants().stream().map(ProductVariantRequest::getVariantOption).toList();
        Set<Long> optionValueIds = variantOptionValues.stream()
                .flatMap(List::stream)
                .map(VariantOptionValueRequest::getOptionValueId)
                .collect(Collectors.toSet());
        List<OptionValues> optionValues = findOptionValues(optionValueIds);
        Map<Long, OptionValues> optionValueById = optionValues.stream().collect(Collectors.toMap(OptionValues::getId, Function.identity()));
        Map<Long, OptionTypes> optionTypeById = optionTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, Function.identity()));

        return new ProductCreationData(categories, optionTypeById, optionValueById);
    }

    public ProductVariantCreationData validateProductVariant(ProductVariantRequest request, Products product){
        validateDuplicateSku(request.getSku());


        Set<Long> productOptionTypeIds = product.getProductOptionTypes().stream().map(ProductOptionTypes::getId).collect(Collectors.toSet());

        List<Long> requestOptionTypeIds = request.getVariantOption().stream().map(VariantOptionValueRequest::getOptionTypeId).toList();

        if(productOptionTypeIds.size() != requestOptionTypeIds.size()){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }

        Set<Long> requestIdSet = new HashSet<>(requestOptionTypeIds);
        if(!productOptionTypeIds.equals(requestIdSet)){
            throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
        }

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

    //TODO 도메인에서 검증되도록 로직 수정됨 삭제 예정
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

    //TODO 도메인에서 검증되도록 로직 수정됨 삭제 예정
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

    private List<OptionValues> findOptionValues(Set<Long> optionValueIds){
        return findOptionValueByIdInOrThrow(optionValueIds);
    }

    private List<OptionTypes> findOptionTypeByIdInOrThrow(List<Long> optionTypeIds){
        List<OptionTypes> result = optionTypeRepository.findByIdIn(optionTypeIds);
        if(optionTypeIds.size() != result.size()){
            throw new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND));
        }
        return result;
    }

    private List<OptionValues> findOptionValueByIdInOrThrow(Set<Long> optionValueIds){
        List<OptionValues> result = optionValueRepository.findByIdIn(new ArrayList<>(optionValueIds));
        if(optionValueIds.size() != result.size()){
            throw new NotFoundException(ms.getMessage(OPTION_VALUE_NOT_FOUND));
        }
        return result;
    }
}
