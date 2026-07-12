package com.example.order_service.order.domain.vo;

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
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    public static Orderer of(Long userId, String userName, String phoneNumber) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 아이디는 필수값 입니다");
        }
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("유저 이름은 필수 입니다");
        }
        if (phoneNumber == null || !phoneNumber.matches("^01[016-9]-\\d{3,4}-\\d{4}$")) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다.");
        }
        return new Orderer(userId, userName, phoneNumber);
    }
}
