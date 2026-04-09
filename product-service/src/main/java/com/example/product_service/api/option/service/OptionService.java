package com.example.product_service.api.option.service;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.option.domain.repository.OptionValueRepository;
import com.example.product_service.api.option.service.dto.command.OptionCommand;
import com.example.product_service.api.option.service.dto.result.OptionResult;
import com.example.product_service.api.option.service.dto.result.OptionValueResult;
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

    public OptionResult saveOption(OptionCommand.Create command) {
        validateDuplicateTypeName(command.name());
        OptionType optionType = OptionType.create(command.name(), command.valueNames());
        OptionType savedOptionType = optionTypeRepository.save(optionType);
        return OptionResult.from(savedOptionType);
    }

    public OptionResult getOption(Long optionTypeId) {
        OptionType optionType = findOptionTypeOrThrow(optionTypeId);
        return OptionResult.from(optionType);
    }

    public List<OptionResult> getOptions() {
        List<OptionType> optionTypes = optionTypeRepository.findAll();
        return optionTypes.stream().map(OptionResult::from).toList();
    }

    public OptionResult updateOptionTypeName(OptionCommand.UpdateOptionType command) {
        OptionType optionType = findOptionTypeOrThrow(command.id());
        validateDuplicateTypeName(command.name().trim());
        optionType.rename(command.name());
        return OptionResult.from(optionType);
    }

    public OptionValueResult updateOptionValueName(OptionCommand.UpdateOptionValue command) {
        OptionValue optionValue = findOptionValueOrThrow(command.id());
        validateDuplicateValueName(optionValue.getOptionType(), command.name());
        optionValue.rename(command.name());
        return OptionValueResult.from(optionValue);
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
