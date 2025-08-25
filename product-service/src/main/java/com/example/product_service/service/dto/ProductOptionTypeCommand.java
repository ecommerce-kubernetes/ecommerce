package com.example.product_service.service.dto;

import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductOptionTypeCommand {
    private Long optionTypeId;
    private int priority;
    private boolean activate;

    public ProductOptionTypeCommand(ProductOptionTypeRequest request){
        this.optionTypeId = request.getOptionTypeId();
        this.priority = request.getPriority();
        this.activate = true;
    }
}
