package com.example.product_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReviewStats {
    private Long reviewCount;
    private Double avgRating;
}
