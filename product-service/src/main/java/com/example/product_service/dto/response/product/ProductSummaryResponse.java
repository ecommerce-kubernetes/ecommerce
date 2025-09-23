package com.example.product_service.dto.response.product;

import com.example.product_service.entity.ProductSummary;
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
    private int minimumPrice;
    private int discountPrice;
    private int discountRate;

    public ProductSummaryResponse(ProductSummary productSummary){
        this.id = productSummary.getId();
        this.name = productSummary.getName();
        this.description = productSummary.getDescription();
        this.thumbnail = productSummary.getThumbnail();
        this.categoryId = productSummary.getCategoryId();
        this.createdAt = productSummary.getCreateAt();
        this.ratingAvg = productSummary.getAvgRating();
        this.reviewCount = productSummary.getReviewCount();
        this.minimumPrice = productSummary.getMinimumPrice();
        this.discountPrice = productSummary.getDiscountedPrice();
        this.discountRate = productSummary.getDiscountRate();
    }
}
