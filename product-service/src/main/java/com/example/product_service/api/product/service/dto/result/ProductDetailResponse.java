package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductOption;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
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
    private Double popularityScore;
    private List<OptionGroup> optionGroups;
    private List<ProductImageResponse> images;
    private List<VariantResponse> variants;

    @Getter
    public static class OptionGroup {
        private Long optionTypeId;
        private String name;
        private Integer priority;
        private List<OptionValueResponse> values;

        @Builder
        private OptionGroup(Long optionTypeId, String name, Integer priority, List<OptionValueResponse> values) {
            this.optionTypeId = optionTypeId;
            this.name = name;
            this.priority = priority;
            this.values = values;
        }

        public static OptionGroup from(ProductOption option) {
            List<OptionValueResponse> optionValues = option.getOptionType().getOptionValues().stream().map(OptionValueResponse::from)
                    .toList();
            return OptionGroup.builder()
                    .optionTypeId(option.getOptionType().getId())
                    .name(option.getOptionType().getName())
                    .priority(option.getPriority())
                    .values(optionValues)
                    .build();
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

        public static OptionValueResponse from(OptionValue optionValue) {
            return OptionValueResponse.builder()
                    .optionValueId(optionValue.getId())
                    .name(optionValue.getName())
                    .build();
        }

    }

    @Builder
    private ProductDetailResponse(Long productId, String name, String status, Long categoryId, String description, Long displayPrice,
                                  Long originalPrice, Integer maxDiscountRate, Double rating, Long reviewCount, Double popularityScore,
                                  List<OptionGroup> optionGroups, List<ProductImageResponse> images, List<VariantResponse> variants) {
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
        this.popularityScore = popularityScore;
        this.optionGroups = optionGroups;
        this.images = images;
        this.variants = variants;
    }

    public static ProductDetailResponse from(Product product) {
        List<OptionGroup> optionGroups = product.getOptions().stream().map(OptionGroup::from)
                .toList();
        List<ProductImageResponse> images = product.getImages().stream().map(ProductImageResponse::from)
                .toList();
        List<VariantResponse> variants = product.getVariants().stream().map(VariantResponse::from).toList();
        return ProductDetailResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .status(product.getStatus().toString())
                .categoryId(product.getCategory().getId())
                .description(product.getDescription())
                .displayPrice(product.getLowestPrice())
                .originalPrice(product.getOriginalPrice())
                .maxDiscountRate(product.getMaxDiscountRate())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .popularityScore(product.getPopularityScore())
                .optionGroups(optionGroups)
                .images(images)
                .variants(variants)
                .build();
    }
}
