package com.example.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
    private Long userId;
    private String email;
    private String name;
    private String birthDate;
    private String gender;
    private String phoneNumber;
    private Boolean phoneVerified;
    private LocalDateTime createdAt;
    private List<ResponseAddress> addresses;
    private Integer cache;
    private Integer point;
    private String role;

    @Builder
    public ResponseUser(Long userId, String email, String name, String birthDate, String gender, String phoneNumber, Boolean phoneVerified, LocalDateTime createdAt, List<ResponseAddress> addresses, Integer cache, Integer point, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.phoneVerified = phoneVerified;
        this.createdAt = createdAt;
        this.addresses = addresses;
        this.cache = cache;
        this.point = point;
        this.role = role;
    }
}
