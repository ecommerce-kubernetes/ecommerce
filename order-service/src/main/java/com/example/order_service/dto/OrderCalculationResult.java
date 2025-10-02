package com.example.order_service.dto;

import com.example.order_service.service.client.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class OrderCalculationResult {
    private Map<Long, Integer> quantityMap;
    private Map<Long, ProductResponse> productByVariantId;
    private long originOrderItemPrice;
    private long productDiscountAmount;
    private long discountedOrderItemsPrice;
    private long couponDiscount;
    private long amountToPay;
}
