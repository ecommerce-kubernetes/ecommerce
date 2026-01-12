package com.example.product_service.api.product.service.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private String status;
    private Long categoryId;

    private Long displayPrice;
    private Long originalPrice;
    private Integer discountRate;

    private Double rating;
    private Long reviewCount;
    private List<OptionGroup> optionGroups;

    private List<ProductImageResponse> images;

    private List<VariantResponse> variantResponse;

    public static class ProductImageResponse {
        private Long imageId;
        private String imageUrl;
        private Integer order;
        private boolean isThumbnail;
    }

    public static class OptionGroup {
        private Long optionTypeId;
        private String name;
        private List<OptionValueResponse> values;

        public static class OptionValueResponse {
            private Long optionValueId;
            private String name;
        }
    }

    public static class VariantResponse {
        private Long variantId;
        private String sku;
        private Long originalPrice;
        private Long discountedPrice;
        private Long discountRate;
        private Integer stockQuantity;
        private List<Long> combination;
    }
}
