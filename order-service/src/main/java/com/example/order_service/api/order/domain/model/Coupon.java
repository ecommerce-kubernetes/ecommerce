package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.domain.model.vo.CouponInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long couponId;
    private String couponName;
    private Long discountAmount;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    private Coupon(Long couponId, String couponName, Long discountAmount) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
    }

    protected void setOrder(Order order){
        this.order = order;
    }

    public static Coupon create(CouponInfo coupon){
        return Coupon.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .discountAmount(coupon.getDiscountAmount())
                .build();
    }
}
