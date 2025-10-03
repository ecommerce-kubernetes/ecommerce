package com.example.order_service.service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserBalanceResponse {
    private Long userId;
    private Long cashAmount;
    private Long pointAmount;
}
