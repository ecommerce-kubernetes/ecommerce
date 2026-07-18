package com.example.order_service.order.application.service.ordersheet.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSheetResult(
        String orderSheetId,
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<OrderSheetItemResult> items,
        CartCouponResult cartCoupon,
        PaymentSummaryResult paymentSummary,

        LocalDateTime expiresAt

) {

    @Builder
    public record OrderSheetItemResult(
            String orderSheetItemId,
            int quantity,
            ProductSnapshot product,
            List<ProductOptionSnapshot> options,
            ItemPriceResult price,
            ItemCouponResult coupon
    ) {
    }

    @Builder
    public record CartCouponResult(
            Long cartCouponId,
            String name,
            Money appliedDiscountAmount
    ) {
    }

    @Builder
    public record PaymentSummaryResult(
            Money totalOriginalAmount,
            Money totalItemDiscount,
            Money totalItemCouponDiscount,
            Money cartCouponDiscount,
            Money usedPoints,
            Money totalPaymentAmount
    ) {
    }

    @Builder
    public record ItemPriceResult(
            Money unitOriginalPrice,
            Money unitDiscountedPrice,
            Money lineTotal,
            Money finalAmount
    ) {
    }

    @Builder
    public record ItemCouponResult(
            Long itemCouponId,
            String name,
            Money appliedDiscountAmount
    ) {
    }
}
