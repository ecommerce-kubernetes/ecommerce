package com.example.order_service.dto;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class OrderCalculationResult {
    private Map<Long, Integer> quantityMap;
    private Map<Long, CartProductResponse> productByVariantId;
    private long originOrderItemPrice;
    private long productDiscountAmount;
    private long discountedOrderItemsPrice;
    private long couponDiscount;
    private long amountToPay;
}
