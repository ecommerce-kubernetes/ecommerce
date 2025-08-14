package com.example.couponservice.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResponseUser {
    private Long userId;
    private String email;
    private String name;
    private String birthDate;
    private String gender;
    private String phoneNumber;
    private boolean isPhoneVerified;
    private LocalDateTime createdAt;
    private Integer cache;
    private Integer point;

    @Builder
    public ResponseUser(Long userId, String email, String name, String birthDate, String gender, String phoneNumber, boolean isPhoneVerified, LocalDateTime createdAt, Integer cache, Integer point) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.isPhoneVerified = isPhoneVerified;
        this.createdAt = createdAt;
        this.cache = cache;
        this.point = point;
    }
}
