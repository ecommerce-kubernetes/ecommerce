package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.dto.response.options.OptionValuesResponse;

public interface OptionValueService {
    OptionValuesResponse saveOptionValues(OptionValueRequest requestDto);
    OptionValuesResponse modifyOptionValues(Long optionValueId, UpdateOptionValueRequest requestDto);
}
