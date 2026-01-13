package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private String status;
    private Long categoryId;
    private String description;
    private Long displayPrice;
    private Long originalPrice;
    private Integer maxDiscountRate;
    private Double rating;
    private Long reviewCount;
    private List<OptionGroup> optionGroups;
    private List<ProductImageResponse> images;
    private List<VariantResponse> variants;

    @Getter
    public static class OptionGroup {
        private Long optionTypeId;
        private String name;
        private List<OptionValueResponse> values;

        @Builder
        private OptionGroup(Long optionTypeId, String name, List<OptionValueResponse> values) {
            this.optionTypeId = optionTypeId;
            this.name = name;
            this.values = values;
        }
    }

    @Getter
    public static class OptionValueResponse {
        private Long optionValueId;
        private String name;

        @Builder
        private OptionValueResponse(Long optionValueId, String name) {
            this.optionValueId = optionValueId;
            this.name = name;
        }
    }

    @Builder
    private ProductDetailResponse(Long productId, String name, String status, Long categoryId, String description, Long displayPrice,
                                 Long originalPrice, Integer maxDiscountRate, Double rating, Long reviewCount, List<OptionGroup> optionGroups,
                                 List<ProductImageResponse> images, List<VariantResponse> variants) {
        this.productId = productId;
        this.name = name;
        this.status = status;
        this.categoryId = categoryId;
        this.description = description;
        this.displayPrice = displayPrice;
        this.originalPrice = originalPrice;
        this.maxDiscountRate = maxDiscountRate;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.optionGroups = optionGroups;
        this.images = images;
        this.variants = variants;
    }
}
