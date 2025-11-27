package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UnitPriceInfo {
    private int originalPrice;
    private int discountRate;
    private int discountAmount;
    private int discountedPrice;
}
