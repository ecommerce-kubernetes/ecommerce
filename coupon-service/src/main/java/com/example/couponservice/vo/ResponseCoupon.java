package com.example.couponservice.vo;

import com.example.couponservice.jpa.entity.DiscountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseCoupon {
    private Long id;
    private String name;
    private String description;
    private String code;
    private String category;
    private DiscountType discountType;
    private int discountValue;
    private int minPurchaseAmount;
    private int maxDiscountAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean reusable;

    @Builder
    public ResponseCoupon(Long id, String name, String description, String code, String category, DiscountType discountType, int discountValue, int minPurchaseAmount, int maxDiscountAmount, LocalDateTime validFrom, LocalDateTime validTo, boolean reusable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.code = code;
        this.category = category;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minPurchaseAmount = minPurchaseAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.reusable = reusable;
    }
}
