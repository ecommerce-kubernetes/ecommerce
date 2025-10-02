package com.example.userservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ResponseUserBalance {
    private Long userId;
    private Long cashAmount;
    private Long pointAmount;
}
