package com.example.product_service.api.product.controller.dto.response;

import com.example.product_service.api.product.service.dto.result.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {

    @Builder
    public record Create(
            Long productId
    ) {
        public static Create from(ProductResult.Create result) {
            return Create.builder()
                    .productId(result.productId())
                    .build();
        }
    }

    @Builder
    public record OptionRegister(
            Long productId,
            List<OptionInfo> options
    ) {
        public static OptionRegister from(ProductResult.OptionRegister result) {
            List<OptionInfo> optionInfos = mappingOptionInfo(result.options());
            return OptionRegister.builder()
                    .productId(result.productId())
                    .options(optionInfos)
                    .build();
        }

        private static List<OptionInfo> mappingOptionInfo(List<ProductResult.Option> options) {
            return options.stream().map(OptionInfo::from).toList();
        }
    }

    @Builder
    public record OptionInfo (
            Long optionTypeId,
            String optionTypeName,
            Integer priority
    ) {
        public static OptionInfo from(ProductResult.Option option) {
           return OptionInfo.builder()
                   .optionTypeId(option.optionTypeId())
                   .optionTypeName(option.optionTypeName())
                   .priority(option.priority())
                   .build();
        }
    }

    @Builder
    public record AddVariant(
            Long productId,
            List<VariantDetail> variants
    ) {
        public static AddVariant from(ProductResult.AddVariant result) {
            List<VariantDetail> variants = mappingVariants(result.variants());
            return ProductResponse.AddVariant.builder()
                    .productId(result.productId())
                    .variants(variants)
                    .build();
        }

        private static List<VariantDetail> mappingVariants(List<ProductResult.VariantDetail> variants) {
            return variants.stream().map(VariantDetail::from).toList();
        }
    }

    @Builder
    public record VariantDetail(
            Long variantId,
            String sku,
            List<Long> optionValueIds,
            Long originalPrice,
            Long discountedPrice,
            Integer discountRate,
            Integer stockQuantity
    ) {
        public static VariantDetail from(ProductResult.VariantDetail result) {
            return VariantDetail.builder()
                    .variantId(result.variantId())
                    .sku(result.sku())
                    .optionValueIds(result.optionValueIds())
                    .originalPrice(result.originalPrice())
                    .discountedPrice(result.discountedPrice())
                    .discountRate(result.discountRate())
                    .stockQuantity(result.stockQuantity())
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
