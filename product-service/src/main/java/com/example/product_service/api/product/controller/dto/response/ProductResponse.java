package com.example.product_service.api.product.controller.dto.response;

import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.api.product.service.dto.result.ProductOptionResponse.OptionDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {

    @Builder
    public record CreateResponse (
            Long productId
    ) {
        public static CreateResponse from(ProductCreateResult result) {
            return CreateResponse.builder()
                    .productId(result.getProductId())
                    .build();
        }
    }

    @Builder
    public record OptionRegisterResponse (
            Long productId,
            List<OptionInfo> options
    ) {
        public static OptionRegisterResponse from(ProductOptionResponse result) {
            List<OptionInfo> optionInfos = mappingOptionInfo(result.getOptions());
            return OptionRegisterResponse.builder()
                    .productId(result.getProductId())
                    .options(optionInfos)
                    .build();
        }

        private static List<OptionInfo> mappingOptionInfo(List<OptionDto> options) {
            return options.stream().map(OptionInfo::from).toList();
        }
    }

    @Builder
    public record OptionInfo (
            Long optionTypeId,
            String optionTypeName,
            Integer priority
    ) {
        public static OptionInfo from(OptionDto option) {
           return OptionInfo.builder()
                   .optionTypeId(option.getOptionTypeId())
                   .optionTypeName(option.getOptionTypeName())
                   .priority(option.getPriority())
                   .build();
        }
    }

    @Builder
    public record AddVariantResponse(
            Long productId,
            List<VariantResponse> variants
    ) {
        public static AddVariantResponse from(AddVariantResult result) {
            List<VariantResponse> variants = mappingVariants(result.getVariants());
            return AddVariantResponse.builder()
                    .productId(result.getProductId())
                    .variants(variants)
                    .build();
        }

        private static List<VariantResponse> mappingVariants(List<VariantResult> variants) {
            return variants.stream().map(VariantResponse::from).toList();
        }
    }

    @Builder
    public record VariantResponse(
            Long variantId,
            String sku,
            List<Long> optionValueIds,
            Long originalPrice,
            Long discountedPrice,
            Integer discountRate,
            Integer stockQuantity
    ) {
        public static VariantResponse from(VariantResult result) {
            return VariantResponse.builder()
                    .variantId(result.getVariantId())
                    .sku(result.getSku())
                    .optionValueIds(result.getOptionValueIds())
                    .originalPrice(result.getOriginalPrice())
                    .discountedPrice(result.getDiscountedPrice())
                    .discountRate(result.getDiscountRate())
                    .stockQuantity(result.getStockQuantity())
                    .build();
        }
    }

    @Builder
    public record AddImageResponse(
            Long productId,
            List<ImageResponse> images
    ) {
        public static AddImageResponse from(ProductImageCreateResult result) {
            List<ImageResponse> images = mappingImageResponse(result.getImages());
            return AddImageResponse.builder()
                    .productId(result.getProductId())
                    .images(images)
                    .build();
        }

        public static List<ImageResponse> mappingImageResponse(List<ProductImageResult> images) {
            return images.stream().map(ImageResponse::from).toList();
        }
    }

    @Builder
    public record ImageResponse(
            Long imageId,
            String imagePath,
            Boolean isThumbnail,
            Integer sortOrder
    ) {
        public static ImageResponse from(ProductImageResult image) {
            return ImageResponse.builder()
                    .imageId(image.getImageId())
                    .imagePath(image.getImagePath())
                    .isThumbnail(image.isThumbnail())
                    .sortOrder(image.getSortOrder())
                    .build();
        }
    }

    @Builder
    public record AddDescriptionImageResponse(
            Long productId,
            List<DescriptionImageResponse> descriptionImages
    ) {
        public static AddDescriptionImageResponse from(ProductDescriptionImageResult result) {
            List<DescriptionImageResponse> images = mappingDescriptionImages(result.getDescriptionImages());
            return AddDescriptionImageResponse.builder()
                    .productId(result.getProductId())
                    .descriptionImages(images)
                    .build();
        }

        private static List<DescriptionImageResponse> mappingDescriptionImages(List<ProductDescriptionImageResponse> images) {
            return images.stream().map(DescriptionImageResponse::from).toList();
        }
    }

    @Builder
    public record DescriptionImageResponse(
            Long imageId,
            String imagePath,
            Integer sortOrder
    ) {
        public static DescriptionImageResponse from(ProductDescriptionImageResponse image) {
            return DescriptionImageResponse.builder()
                    .imageId(image.getImageId())
                    .imagePath(image.getImagePath())
                    .sortOrder(image.getSortOrder())
                    .build();
        }
    }

    @Builder
    public record PublishResponse (
            Long productId,
            String status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime publishedAt
    ) {
        public static PublishResponse from(ProductStatusResult result) {
            return PublishResponse.builder()
                    .productId(result.getProductId())
                    .status(result.getStatus())
                    .publishedAt(result.getPublishedAt())
                    .build();
        }
    }

    @Builder
    public record CloseResponse (
            Long productId,
            String status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime saleStoppedAt
    ) {
        public static CloseResponse from(ProductStatusResult result) {
            return CloseResponse.builder()
                    .productId(result.getProductId())
                    .status(result.getStatus())
                    .saleStoppedAt(result.getSaleStoppedAt())
                    .build();
        }
    }

    @Builder
    public record UpdateResponse (
            Long productId,
            String name,
            String description,
            Long categoryId
    ) {
        public static UpdateResponse from (ProductUpdateResponse result) {
            return UpdateResponse.builder()
                    .productId(result.getProductId())
                    .name(result.getName())
                    .description(result.getDescription())
                    .categoryId(result.getCategoryId())
                    .build();
        }
    }
}
