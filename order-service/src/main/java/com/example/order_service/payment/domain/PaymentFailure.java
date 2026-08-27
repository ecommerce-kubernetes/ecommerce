package com.example.order_service.payment.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentFailure {
    private String code;
    private String message;

    private PaymentFailure(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static PaymentFailure of(String code, String message) {
        Assert.hasText(code, "결제 실패 코드는 필수 입니다.");
        Assert.hasText(message, "결제 실패 메시지는 필수 입니다.");

        return new PaymentFailure(code, message);
    }
}
