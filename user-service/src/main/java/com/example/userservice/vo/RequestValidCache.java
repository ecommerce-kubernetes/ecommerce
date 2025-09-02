package com.example.userservice.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestValidCache {

    private Long userId;

    private int reservedPointAmount;

    private int reservedCacheAmount;
}
