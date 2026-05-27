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
        return new Orderer(userId, userName, phoneNumber);
    }
}
