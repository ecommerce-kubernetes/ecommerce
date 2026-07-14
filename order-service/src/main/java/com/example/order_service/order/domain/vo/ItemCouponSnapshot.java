package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCouponSnapshot {
    private Long itemCouponId;
    private String itemCouponName;
    private Money discountAmount;

    @Builder(builderMethodName = "reconstitute")
    private ItemCouponSnapshot(Long itemCouponId, String itemCouponName, Money discountAmount) {
        this.itemCouponId = Objects.requireNonNull(itemCouponId, "상품 쿠폰 아이디는 필수입니다.");
        this.itemCouponName = Objects.requireNonNull(itemCouponName, "상품 쿠폰 이름은 필수입니다.");
        this.discountAmount = Objects.requireNonNull(discountAmount, "상품 쿠폰 할인 가격은 필수입니다.");
    }

    public static ItemCouponSnapshot of(Long itemCouponId, String itemCouponName, Money discountAmount) {
        return new ItemCouponSnapshot(itemCouponId, itemCouponName, discountAmount);
    }

    public static ItemCouponSnapshot empty() {
        //TODO 변경요함
        return new ItemCouponSnapshot(999L, "쿠폰 미적용", Money.ZERO);
    }
}
