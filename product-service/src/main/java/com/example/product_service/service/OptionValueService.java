package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;

public interface OptionValueService {
    OptionValueResponse updateOptionValueById(Long optionValueId, OptionValueRequest requestDto);
    OptionValueResponse getOptionValueById(Long optionValueId);
    void deleteOptionValueById(Long optionValueId);
}
