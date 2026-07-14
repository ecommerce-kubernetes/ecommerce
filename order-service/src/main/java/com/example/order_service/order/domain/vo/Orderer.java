package com.example.order_service.order.domain.vo;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import com.mysema.commons.lang.Assert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orderer {
    private Long userId;
    private String userName;
    private String phoneNumber;

    @Builder(builderMethodName = "reconstitute")
    private Orderer(Long userId, String userName, String phoneNumber) {
        Assert.notNull(userId, "유저 아이디는 필수 입니다.");
        Assert.hasText(userName, "유저 이름은 필수 입니다.");
        Assert.hasText(phoneNumber, "유저 전화번호는 필수 입니다.");

        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    public static Orderer of(Long userId, String userName, String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("^01[016-9]-\\d{3,4}-\\d{4}$")) {
            throw new BusinessException(OrderErrorCode.INVALID_PHONE_NUMBER);
        }
        return new Orderer(userId, userName, phoneNumber);
    }
}
