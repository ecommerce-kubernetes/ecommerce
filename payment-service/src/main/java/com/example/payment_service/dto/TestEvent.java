package com.example.payment_service.dto;

import com.example.common.Product;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEvent {
    private Long orderId;
    private Long userId;
    private Long userCouponId;
    private boolean pointUsage;
    private int reservedPointAmount;
    private int reservedCacheAmount;
    private int expectTotalAmount;
}
