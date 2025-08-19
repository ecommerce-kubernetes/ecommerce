package com.example.product_service.dto.response.options;

import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.ProductOptionTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionTypeResponse {
    private Long id;
    private String name;

    public ProductOptionTypeResponse(OptionTypes optionTypes){
        this.id = optionTypes.getId();
        this.name = optionTypes.getName();
    }
}
