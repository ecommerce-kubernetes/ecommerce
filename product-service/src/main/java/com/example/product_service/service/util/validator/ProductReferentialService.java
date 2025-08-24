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
        //OptionType 조회
        Map<Long, OptionTypes> optionTypeById = findOptionTypesMap(request);
        //OptionValue 조회
        Map<Long, OptionValues> optionValueById = findOptionValuesMap(request);

        return new ProductCreationData(categories, optionTypeById, optionValueById);
    }

    public ProductVariantCreationData validateProductVariant(ProductVariantRequest request){
        //SKU 중복 확인
        validateDuplicateSku(request.getSku());

        //OptionValue 조회
        Map<Long, OptionValues> optionValueById = findOptionValuesMap(request);

        return new ProductVariantCreationData(optionValueById);
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
        ensureSkusDoNotExists(Collections.singletonList(sku));
    }

    private void ensureSkusDoNotExists(Collection<String> skus){
        boolean isConflict = productVariantsRepository.existsBySkuIn(skus);
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
        }
    }

    private Map<Long, OptionTypes> findOptionTypesMap(ProductRequest request){
        List<Long> optionTypeIds = request.getProductOptionTypes()
                .stream().map(ProductOptionTypeRequest::getOptionTypeId).toList();
        List<OptionTypes> optionTypes = findOptionTypeByIdInOrThrow(optionTypeIds);
        return optionTypes.stream().collect(Collectors.toMap(OptionTypes::getId, Function.identity()));
    }

    private Map<Long, OptionValues> findOptionValuesMap(ProductRequest request){
        List<List<VariantOptionValueRequest>> variantOptionValues = request.getProductVariants()
                .stream().map(ProductVariantRequest::getVariantOption).toList();
        Set<Long> optionValueIds = variantOptionValues.stream()
                .flatMap(List::stream)
                .map(VariantOptionValueRequest::getOptionValueId)
                .collect(Collectors.toSet());

        List<OptionValues> optionValues = findOptionValueByIdInOrThrow(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValues::getId, Function.identity()));
    }

    private Map<Long, OptionValues> findOptionValuesMap(ProductVariantRequest request){
        Set<Long> optionValueIds = request.getVariantOption().stream().map(VariantOptionValueRequest::getOptionValueId).collect(Collectors.toSet());
        List<OptionValues> optionValues = findOptionValueByIdInOrThrow(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValues::getId, Function.identity()));
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
