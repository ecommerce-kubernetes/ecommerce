package com.example.product_service.service;

import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.OptionValuesUpdateRequestDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;

public interface OptionValueService {
    OptionValuesResponseDto saveOptionValues(OptionValueRequest requestDto);
    void batchDeleteOptionValues(IdsRequestDto requestDto);
    OptionValuesResponseDto modifyOptionValues(Long optionValueId, OptionValuesUpdateRequestDto requestDto);
}
