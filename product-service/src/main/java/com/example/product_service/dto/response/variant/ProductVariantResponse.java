package com.example.product_service.dto.response.variant;

import com.example.product_service.dto.response.options.OptionValueResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private int price;
    private int discountRate;
    private List<OptionValueResponse> optionValues;
}
