package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;

public class OrderItemAmount {

    private Money originalAmount;

    private Money itemDiscount;

    private Money lineTotal;

    private Money itemCouponDiscount;

    private Money finalAmount;
}
