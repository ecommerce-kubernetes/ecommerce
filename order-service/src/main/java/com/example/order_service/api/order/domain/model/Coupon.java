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
    @Embedded
    private CouponInfo couponInfo;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    private Coupon(CouponInfo couponInfo) {
        this.couponInfo = couponInfo;
    }

    protected void setOrder(Order order){
        this.order = order;
    }

    public static Coupon create(CouponInfo coupon){
        return Coupon.builder()
                .couponInfo(coupon)
                .build();
    }
}
