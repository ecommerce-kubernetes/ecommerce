package com.example.product_service.dto.response.product;

import com.example.product_service.entity.*;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private List<ProductImageDto> images;
    private double ratingAvg;
    private int totalReviewCount;
    private List<Long> optionTypes;
    private List<VariantsResponseDto> variants;

    public ProductResponseDto(Products product){
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
        this.createAt = product.getCreateAt();
        this.updateAt = product.getUpdateAt();
        this.images = product.getImages().stream().sorted(Comparator.comparing(ProductImages::getSortOrder))
                .map(ProductImageDto::new).toList();
        IntSummaryStatistics stats = product.getReviews().stream()
                .mapToInt(Reviews::getRating)
                .summaryStatistics();
        this.totalReviewCount = (int) stats.getCount();
        this.ratingAvg = stats.getCount() == 0
                ? 0.0
                : stats.getAverage();
        this.optionTypes = product.getProductOptionTypes().stream().map((optionTypes) -> optionTypes.getOptionType().getId())
                .toList();
        this.variants = product.getProductVariants().stream().map((VariantsResponseDto::new)).toList();
    }
}
