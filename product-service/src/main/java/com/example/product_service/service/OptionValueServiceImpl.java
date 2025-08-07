package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.dto.response.options.OptionValuesResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.OptionValuesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptionValueServiceImpl implements OptionValueService{

    private final OptionTypesRepository optionTypesRepository;
    private final OptionValuesRepository optionValuesRepository;

    @Override
    @Transactional
    public OptionValuesResponse saveOptionValues(OptionValueRequest requestDto) {
        Long optionTypeId = requestDto.getOptionTypeId();

        OptionTypes optionType = optionTypesRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionTypes"));

        OptionValues optionValues = new OptionValues(requestDto.getValue(),optionType);
        optionType.addOptionValue(optionValues);

        OptionValues saved = optionValuesRepository.save(optionValues);
        return new OptionValuesResponse(
                saved.getId(),
                saved.getOptionValue(),
                optionTypeId
        );
    }

    @Override
    @Transactional
    public OptionValuesResponse modifyOptionValues(Long optionValueId, UpdateOptionValueRequest requestDto) {

        OptionValues optionValue = optionValuesRepository
                .findById(optionValueId).orElseThrow(() -> new NotFoundException("Not Found OptionValue"));

//        optionValue.setOptionValue(requestDto.getOptionValue());

        return new OptionValuesResponse(optionValue);
    }
}
