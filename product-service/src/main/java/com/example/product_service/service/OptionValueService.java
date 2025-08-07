package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;

public interface OptionValueService {
    OptionValueResponse saveOptionValue(Long optionTypeId, OptionValueRequest requestDto);
    OptionValueResponse modifyOptionValues(Long optionValueId, OptionValueRequest requestDto);
    OptionValueResponse getOptionValueById(Long optionValueId);
}
