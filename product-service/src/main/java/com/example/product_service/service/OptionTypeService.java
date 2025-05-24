package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionTypeRequestIdsDto;
import com.example.product_service.dto.request.options.OptionTypesRequestDto;
import com.example.product_service.dto.request.options.OptionTypesResponseDto;
import com.example.product_service.dto.response.PageDto;

import org.springframework.data.domain.Pageable;

public interface OptionTypeService {
    OptionTypesResponseDto saveOptionTypes(OptionTypesRequestDto requestDto);
    PageDto<OptionTypesResponseDto> getOptionTypes(String query, Pageable pageable);
    OptionTypesResponseDto modifyOptionTypes(Long optionTypeId, OptionTypesRequestDto requestDto);
    void deleteOptionTypes(Long optionTypeId);
    void batchDeleteOptionTypes(OptionTypeRequestIdsDto requestDto);
}
