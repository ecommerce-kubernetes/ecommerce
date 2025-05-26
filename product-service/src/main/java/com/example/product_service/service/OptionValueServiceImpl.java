package com.example.product_service.service;

import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValuesRequestDto;
import com.example.product_service.dto.request.options.OptionValuesUpdateRequestDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.OptionValuesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionValueServiceImpl implements OptionValueService{

    private final OptionTypesRepository optionTypesRepository;
    private final OptionValuesRepository optionValuesRepository;

    @Override
    @Transactional
    public OptionValuesResponseDto saveOptionValues(OptionValuesRequestDto requestDto) {
        Long optionTypeId = requestDto.getOptionTypeId();

        OptionTypes optionType = optionTypesRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionTypes"));

        OptionValues optionValues = new OptionValues(requestDto.getOptionValue(),optionType);
        optionType.addOptionValue(optionValues);

        OptionValues saved = optionValuesRepository.save(optionValues);
        return new OptionValuesResponseDto(
                saved.getId(),
                saved.getOptionValue(),
                optionTypeId
        );
    }

    @Override
    @Transactional
    public void batchDeleteOptionValues(IdsRequestDto requestDto) {
        List<Long> ids = new ArrayList<>(requestDto.getIds());
        List<OptionValues> optionValues = optionValuesRepository.findByIdIn(ids);

        List<Long> existIds = optionValues.stream().map(OptionValues::getId).toList();
        ids.removeAll(existIds);
        if(!ids.isEmpty()){
            throw new NotFoundException("Not Found OptionValue ids : " + ids);
        }

        optionValuesRepository.deleteAll(optionValues);
    }

    @Override
    @Transactional
    public OptionValuesResponseDto modifyOptionValues(Long optionValueId, OptionValuesUpdateRequestDto requestDto) {

        OptionValues optionValue = optionValuesRepository
                .findById(optionValueId).orElseThrow(() -> new NotFoundException("Not Found OptionValue"));

        optionValue.setOptionValue(requestDto.getOptionValue());

        return new OptionValuesResponseDto(optionValue);
    }
}
