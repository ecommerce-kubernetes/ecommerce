package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;

import com.example.product_service.dto.response.options.OptionValuesResponse;

import java.util.List;

public interface OptionTypeService {
    OptionTypeResponse saveOptionTypes(OptionTypeRequest requestDto);
    List<OptionTypeResponse> getOptionTypes();
    OptionTypeResponse updateOptionTypeById(Long optionTypeId, OptionTypeRequest request);
    void deleteOptionTypeById(Long optionTypeId);
    List<OptionValuesResponse> getOptionValuesByTypeId(Long optionTypeId);
}
