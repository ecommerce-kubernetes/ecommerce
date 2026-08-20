package com.example.order_service.order.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCancelInfo {

    private String reason;

    private LocalDateTime canceledAt;

    private OrderCancelInfo(String reason, LocalDateTime canceledAt) {
        this.reason = reason;
        this.canceledAt = canceledAt;
    }

    public static OrderCancelInfo of(String reason, LocalDateTime canceledAt) {
        Assert.hasText(reason, "주문 취소 이유는 필수이다.");
        Assert.notNull(canceledAt, "주문 취소 시간은 필수이다.");
        return new OrderCancelInfo(reason, canceledAt);
    }
}
