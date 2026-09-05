package com.example.product_service.option.adapter.in.web.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypesResponse(List<OptionTypeResponse> optionTypes) {
}
