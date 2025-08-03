package com.example.product_service.dto.response.variant;

import com.example.product_service.dto.response.options.OptionValueResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddVariantResponse {
    private Long id;
    private Long productId;
    private String sku;
    private List<OptionValueResponse> optionValues;
    private int price;
    private int stockQuantity;
    private int discountRate;
    private LocalDateTime createdAt;
}
