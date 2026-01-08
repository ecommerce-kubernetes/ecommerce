package com.example.product_service.service.util.validator;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.OptionValueRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductUpdateData;
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
public class ProductReferenceService {
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public ProductCreationData buildCreationData(ProductRequest request){
        //SKU 중복확인
        validateDuplicateSkus(request);
        //카테고리 조회 -> 없으면 예외 던짐
        Category category = findCategoryByIdOrThrow(request.getCategoryId());
        //OptionType 조회
        Map<Long, OptionType> optionTypeById = findOptionTypesMap(request);
        //OptionValue 조회
        Map<Long, OptionValue> optionValueById = findOptionValuesMap(request);

        return new ProductCreationData(category, optionTypeById, optionValueById);
    }

    public ProductVariantCreationData buildVariantCreationData(ProductVariantRequest request){
        //SKU 중복 확인
        validateDuplicateSku(request.getSku());

        //OptionValue 조회
        Map<Long, OptionValue> optionValueById = findOptionValuesMap(request);

        return new ProductVariantCreationData(optionValueById);
    }

    public ProductUpdateData resolveUpdateData(UpdateProductBasicRequest request){
        Category category = findCategoryByIdOrThrow(request.getCategoryId());
        return new ProductUpdateData(category);
    }

    private Category findCategoryByIdOrThrow(Long categoryId){
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));

        return category;
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

    private Map<Long, OptionType> findOptionTypesMap(ProductRequest request){
        List<Long> optionTypeIds = request.getProductOptionTypes()
                .stream().map(ProductOptionTypeRequest::getOptionTypeId).toList();
        List<OptionType> optionTypes = findOptionTypeByIdInOrThrow(optionTypeIds);
        return optionTypes.stream().collect(Collectors.toMap(OptionType::getId, Function.identity()));
    }

    private Map<Long, OptionValue> findOptionValuesMap(ProductRequest request){
        List<List<VariantOptionValueRequest>> variantOptionValues = request.getProductVariants()
                .stream().map(ProductVariantRequest::getVariantOption).toList();
        Set<Long> optionValueIds = variantOptionValues.stream()
                .flatMap(List::stream)
                .map(VariantOptionValueRequest::getOptionValueId)
                .collect(Collectors.toSet());

        List<OptionValue> optionValues = findOptionValueByIdInOrThrow(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValue::getId, Function.identity()));
    }

    private Map<Long, OptionValue> findOptionValuesMap(ProductVariantRequest request){
        Set<Long> optionValueIds = request.getVariantOption().stream().map(VariantOptionValueRequest::getOptionValueId).collect(Collectors.toSet());
        List<OptionValue> optionValues = findOptionValueByIdInOrThrow(optionValueIds);
        return optionValues.stream().collect(Collectors.toMap(OptionValue::getId, Function.identity()));
    }

    private List<OptionType> findOptionTypeByIdInOrThrow(List<Long> optionTypeIds){
        List<OptionType> result = optionTypeRepository.findByIdIn(optionTypeIds);
        if(optionTypeIds.size() != result.size()){
            throw new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND));
        }
        return result;
    }

    private List<OptionValue> findOptionValueByIdInOrThrow(Set<Long> optionValueIds){
        List<OptionValue> result = optionValueRepository.findByIdIn(new ArrayList<>(optionValueIds));
        if(optionValueIds.size() != result.size()){
            throw new NotFoundException(ms.getMessage(OPTION_VALUE_NOT_FOUND));
        }
        return result;
    }
}
