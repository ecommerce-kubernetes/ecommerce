package com.example.order_service.ordersheet.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.vo.CouponStatus;
import lombok.Builder;

import java.util.List;

public class OrderSheetCouponResult {

    @Builder
    public record Info(
            Long couponId,
            String couponName,
            String scope,
            Boolean available,
            Money discountAmount,
            CouponStatus status,
            List<String> applicableItemIds
    ) {
    }
}
