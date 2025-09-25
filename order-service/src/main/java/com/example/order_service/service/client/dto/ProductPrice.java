package com.example.order_service.service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ProductPrice {
    private long unitPrice;
    private int discountRate;
    private long discountAmount;
    private long discountedPrice;
}
