package com.example.product_service.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private String thumbnail;
    private Long categoryId;
    private LocalDateTime createdAt;
    private double ratingAvg;
    private int reviewCount;
    private int originPrice;
    private int discountPrice;
    private int discountRate;
}
