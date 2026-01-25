package com.example.order_service.api.order.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orderer {
    private Long userId;
    private String userName;
    private String phoneNumber;

    @Builder(access = AccessLevel.PRIVATE)
    private Orderer(Long userId, String userName, String phoneNumber) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    public static Orderer of(Long userId, String userName, String phoneNumber) {
        return Orderer.builder()
                .userId(userId)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .build();
    }
}
