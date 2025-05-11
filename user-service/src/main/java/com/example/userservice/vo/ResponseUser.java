package com.example.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
    private Long userId;
    private String email;
    private String name;
    private LocalDateTime createdAt;
    private List<ResponseAddress> addresses;
    private int cache;
    private int point;

    @Builder
    public ResponseUser(Long userId, String email, String name, LocalDateTime createdAt, List<ResponseAddress> addresses, int cache, int point) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
        this.addresses = addresses;
        this.cache = cache;
        this.point = point;
    }
}
