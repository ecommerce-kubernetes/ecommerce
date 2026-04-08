package com.example.product_service.api.product.controller.dto.response;

import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.service.dto.result.ProductResult;
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
    public record AddImage(
            Long productId,
            List<ImageDetail> images
    ) {
        public static AddImage from(ProductResult.AddImage result) {
            List<ImageDetail> images = mappingImageResponse(result.images());
            return AddImage.builder()
                    .productId(result.productId())
                    .images(images)
                    .build();
        }

        public static List<ImageDetail> mappingImageResponse(List<ProductResult.ImageDetail> images) {
            return images.stream().map(ImageDetail::from).toList();
        }
    }

    @Builder
    public record ImageDetail(
            Long imageId,
            String imagePath,
            Boolean isThumbnail,
            Integer sortOrder
    ) {
        public static ImageDetail from(ProductResult.ImageDetail image) {
            return ImageDetail.builder()
                    .imageId(image.imageId())
                    .imagePath(image.imagePath())
                    .isThumbnail(image.isThumbnail())
                    .sortOrder(image.sortOrder())
                    .build();
        }
    }

    @Builder
    public record AddDescriptionImage(
            Long productId,
            List<DescriptionImageDetail> descriptionImages
    ) {
        public static AddDescriptionImage from(ProductResult.AddDescriptionImage result) {
            List<DescriptionImageDetail> images = mappingDescriptionImages(result.images());
            return AddDescriptionImage.builder()
                    .productId(result.productId())
                    .descriptionImages(images)
                    .build();
        }

        private static List<DescriptionImageDetail> mappingDescriptionImages(List<ProductResult.DescriptionImageDetail> images) {
            return images.stream().map(DescriptionImageDetail::from).toList();
        }
    }

    @Builder
    public record DescriptionImageDetail(
            Long imageId,
            String imagePath,
            Integer sortOrder
    ) {
        public static DescriptionImageDetail from(ProductResult.DescriptionImageDetail image) {
            return DescriptionImageDetail.builder()
                    .imageId(image.imageId())
                    .imagePath(image.imagePath())
                    .sortOrder(image.sortOrder())
                    .build();
        }
    }

    @Builder
    public record Publish(
            Long productId,
            ProductStatus status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime publishedAt
    ) {
        public static Publish from(ProductResult.Publish result) {
            return Publish.builder()
                    .productId(result.productId())
                    .status(result.status())
                    .publishedAt(result.publishedAt())
                    .build();
        }
    }

    @Builder
    public record Close(
            Long productId,
            ProductStatus status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime saleStoppedAt
    ) {
        public static Close from(ProductResult.Close result) {
            return Close.builder()
                    .productId(result.productId())
                    .status(result.status())
                    .saleStoppedAt(result.saleStoppedAt())
                    .build();
        }
    }

    @Builder
    public record Update(
            Long productId,
            String name,
            String description,
            Long categoryId
    ) {
        public static Update from (ProductResult.Update result) {
            return Update.builder()
                    .productId(result.productId())
                    .name(result.name())
                    .description(result.description())
                    .categoryId(result.categoryId())
                    .build();
        }
    }
}
