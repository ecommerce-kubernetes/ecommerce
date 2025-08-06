package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.PageDto;

import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OptionTypeService {
    OptionTypeResponse saveOptionTypes(OptionTypeRequest requestDto);
    PageDto<OptionTypeResponse> getOptionTypes(String query, Pageable pageable);
    OptionTypeResponse modifyOptionTypes(Long optionTypeId, OptionTypeRequest requestDto);
    void deleteOptionTypes(Long optionTypeId);
    List<OptionValuesResponseDto> getOptionValuesByTypeId(Long optionTypeId);
}
