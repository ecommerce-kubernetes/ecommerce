package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;

public class OrderAmount {

    private Money totalOriginalAmount;

    private Money totalItemDiscount;

    private Money totalItemCouponDiscount;

    private Money cartCouponDiscount;

    private Money usedPoints;

    private Money totalPaymentAmount;
}
