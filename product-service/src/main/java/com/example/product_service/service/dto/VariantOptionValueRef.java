package com.example.product_service.service.dto;

import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VariantOptionValueRef {
    private Long optionTypeId;
    private Long optionValueId;

    public VariantOptionValueRef(VariantOptionValueRequest request){
        this.optionTypeId = request.getOptionTypeId();
        this.optionValueId = request.getOptionValueId();
    }
}
