package com.example.product_service.dto.response.product;

import com.example.product_service.entity.*;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private List<OptionDto> options;
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
        Set<Long> usedValueIds = product.getProductVariants().stream()
                .flatMap(v -> v.getProductVariantOptions().stream())
                .map(pvo -> pvo.getOptionValue().getId())
                .collect(Collectors.toSet());

        // 2) options 매핑 및 필터링
        this.options = product.getProductOptionTypes().stream()
                .map(pot -> {
                    OptionTypes ot = pot.getOptionType();
                    List<OptionValueDto> vals = ot.getOptionValues().stream()
                            .filter(ov -> usedValueIds.contains(ov.getId()))
                            .map(ov -> new OptionValueDto(ov.getId(), ov.getOptionValue()))
                            .collect(Collectors.toList());
                    return new OptionDto(ot.getId(), ot.getName(), vals);
                })
                .collect(Collectors.toList());
        this.variants = product.getProductVariants().stream().map((VariantsResponseDto::new)).toList();
    }
    @Getter
    @AllArgsConstructor
    public static class OptionDto {
        private Long id;
        private String name;
        private List<OptionValueDto> values;
    }

    @Getter
    @AllArgsConstructor
    public static class OptionValueDto {
        private Long id;
        private String optionValue;
    }
}
