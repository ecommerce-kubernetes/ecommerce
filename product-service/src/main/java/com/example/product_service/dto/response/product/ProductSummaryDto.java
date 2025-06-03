package com.example.product_service.dto.response.product;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@ToString
@Setter
@NoArgsConstructor
public class ProductSummaryDto {
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private String categoryName;
    private double ratingAvg;
    private int totalReviewCount;
    private int originPrice;
    private int discountPrice;
    private int discountValue;

    @QueryProjection
    public ProductSummaryDto(Long id, String name, String description,
                             String thumbnailUrl, String categoryName,
                             double ratingAvg, int totalReviewCount, int originPrice, int discountPrice, int discountValue){
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryName = categoryName;
        this.ratingAvg = ratingAvg;
        this.totalReviewCount = totalReviewCount;
        this.originPrice = originPrice;
        this.discountPrice = discountPrice;
        this.discountValue = discountValue;
    }
}
