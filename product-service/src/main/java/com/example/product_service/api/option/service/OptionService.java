package com.example.product_service.api.option.service;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.option.service.dto.OptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionService {

    private final OptionTypeRepository optionTypeRepository;

    public OptionResponse saveOption(String name, List<String> values) {
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

    public OptionResponse updateOption(Long optionTypeId, String name, List<String> values) {
        OptionType optionType = findOptionTypeOrThrow(optionTypeId);
        optionType.update(name, values);
        return OptionResponse.from(optionType);
    }

    public void deleteOption(Long optionTypeId) {
        OptionType optionType = findOptionTypeOrThrow(optionTypeId);
        optionTypeRepository.delete(optionType);
    }

    private OptionType findOptionTypeOrThrow(Long optionTypeId) {
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_NOT_FOUND));
    }
}
