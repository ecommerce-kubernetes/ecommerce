package com.example.product_service.option.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.option.exception.OptionErrorCode;
import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import com.example.product_service.option.domain.OptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionQueryService {

    private final OptionTypeRepository optionTypeRepository;

    public OptionTypesResult getTypes() {
        List<OptionType> optionTypes = optionTypeRepository.findAll();
        return OptionTypesResult.from(optionTypes);
    }

    public OptionTypeResult getType(Long optionTypeId) {
        OptionType optionType = optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_TYPE_NOT_FOUND));
        return OptionTypeResult.from(optionType);
    }
}
