package com.example.couponservice.dto;

import com.example.couponservice.jpa.entity.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
public class CouponDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String category;
    private DiscountType discountType;
    private int discountValue;
    private int minPurchaseAmount;
    private int maxDiscountAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean reusable;
}
