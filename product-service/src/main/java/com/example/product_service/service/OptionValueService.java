package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;

public interface OptionValueService {
    OptionValueResponse saveOptionValue(Long optionTypeId, OptionValueRequest requestDto);
    OptionValueResponse modifyOptionValues(Long optionValueId, UpdateOptionValueRequest requestDto);
    OptionValueResponse getOptionValueById(Long optionValueId);
}
