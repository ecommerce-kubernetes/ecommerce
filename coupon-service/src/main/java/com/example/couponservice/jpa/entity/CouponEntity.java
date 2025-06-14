package com.example.couponservice.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true)
    private String code;
    @Column
    private String description;
    @Column
    private String category;
    @Column(nullable = false)
    private DiscountType discountType;
    @Column(nullable = false)
    private int discountValue;
    @Column(nullable = false)
    private int minPurchaseAmount;
    @Column(nullable = false)
    private int maxDiscountAmount;
    @Column
    private LocalDateTime validFrom;
    @Column
    private LocalDateTime validTo;

    @Column(nullable = false)
    private boolean reusable;

    @Builder
    public CouponEntity(String name, String code, String description, String category, DiscountType discountType, int discountValue, int minPurchaseAmount, int maxDiscountAmount, LocalDateTime validFrom, LocalDateTime validTo, boolean reusable) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.category = category;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minPurchaseAmount = minPurchaseAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.reusable = reusable;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeCategory(String category) {
        this.category = category;
    }

    public void changeDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public void changeDiscountValue(int discountValue) {
        this.discountValue = discountValue;
    }

    public void changeMinPurchaseAmount(int minPurchaseAmount) {
        this.minPurchaseAmount = minPurchaseAmount;
    }

    public void changeMaxDiscountAmount(int maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public void changeValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public void changeValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public void changeReusable(boolean reusable) {
        this.reusable = reusable;
    }


}
