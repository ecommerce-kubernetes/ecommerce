package com.example.product_service.api.option.service;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.option.domain.repository.OptionValueRepository;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import com.example.product_service.api.product.domain.repository.ProductOptionRepository;
import com.example.product_service.api.product.domain.repository.ProductVariantOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionService {

    private final OptionTypeRepository optionTypeRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductVariantOptionRepository productVariantOptionRepository;

    public OptionResponse saveOption(String name, List<String> values) {
        validateDuplicateTypeName(name);
        OptionType optionType = OptionType.create(name, values);
        OptionType savedOptionType = optionTypeRepository.save(optionType);
        return OptionResponse.from(savedOptionType);
    }

    public OptionResponse getOption(Long optionTypeId) {
        OptionType optionType = findOptionTypeOrThrow(optionTypeId);
        return OptionResponse.from(optionType);
    }

    public List<OptionResponse> getOptions() {
        List<OptionType> optionTypes = optionTypeRepository.findAll();
        return optionTypes.stream().map(OptionResponse::from).toList();
    }

    public OptionResponse updateOptionTypeName(Long optionTypeId, String name) {
        String trimName = name.trim();
        OptionType optionType = findOptionTypeOrThrow(optionTypeId);
        validateDuplicateTypeName(trimName);
        optionType.rename(trimName);
        return OptionResponse.from(optionType);
    }

    //TODO 옵션 값이 존재하는지 확인
    public OptionValueResponse updateOptionValueName(Long optionValueId, String name) {
        OptionValue optionValue = findOptionValueOrThrow(optionValueId);
        validateDuplicateValueName(optionValue.getOptionType(), name);
        optionValue.rename(name);
        return OptionValueResponse.from(optionValue);
    }

    public void deleteOption(Long optionTypeId) {
        OptionType optionType = findOptionTypeOrThrow(optionTypeId);
        validateProductOption(optionType.getId());
        optionTypeRepository.delete(optionType);
    }

    public void deleteOptionValue(Long optionValueId) {
        OptionValue optionValue = findOptionValueOrThrow(optionValueId);
        validateProductVariantOption(optionValue.getId());
        optionValueRepository.delete(optionValue);
    }

    private OptionType findOptionTypeOrThrow(Long optionTypeId) {
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_NOT_FOUND));
    }

    private OptionValue findOptionValueOrThrow(Long optionValueId) {
        return optionValueRepository.findById(optionValueId)
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_VALUE_NOT_FOUND));
    }

    private void validateDuplicateTypeName(String name) {
        if (optionTypeRepository.existsByName(name)){
            throw new BusinessException(OptionErrorCode.DUPLICATE_NAME);
        }
    }

    private void validateDuplicateValueName(OptionType optionType, String name) {
        if (optionValueRepository.existsByOptionTypeAndName(optionType, name)){
            throw new BusinessException(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
        }
    }

    private void validateProductOption(Long optionTypeId) {
        if (productOptionRepository.existByOptionTypeId(optionTypeId)) {
            throw new BusinessException(OptionErrorCode.OPTION_IN_PRODUCT_OPTION);
        }
    }

    private void validateProductVariantOption(Long optionValueId) {
        if (productVariantOptionRepository.existByOptionValueId(optionValueId)){
            throw new BusinessException(OptionErrorCode.OPTION_VALUE_IN_VARIANT);
        }
    }
}
