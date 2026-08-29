package com.example.userservice.user.adapter.in.web.dto;

import com.example.userservice.user.application.service.dto.result.AddShippingAddressResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record AddShippingAddressResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long userId
) {
    public static AddShippingAddressResponse from(AddShippingAddressResult result) {
        return AddShippingAddressResponse.builder()
                .userId(result.userId())
                .build();
    }
}
