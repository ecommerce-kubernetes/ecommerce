package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
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
    public OptionValueResponse saveOptionValue(Long optionTypeId, OptionValueRequest request) {
        return new OptionValueResponse();
    }

    @Override
    @Transactional
    public OptionValueResponse modifyOptionValues(Long optionValueId, UpdateOptionValueRequest requestDto) {

        OptionValues optionValue = optionValuesRepository
                .findById(optionValueId).orElseThrow(() -> new NotFoundException("Not Found OptionValue"));

//        optionValue.setOptionValue(requestDto.getOptionValue());

        return new OptionValueResponse();
    }
}
