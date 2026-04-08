package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResult {

    @Builder
    public record Create (
            Long productId
    ) {
        public static Create from (Product product) {
            return Create.builder()
                    .productId(product.getId())
                    .build();
        }
    }

    @Builder
    public record OptionRegister (
            Long productId,
            List<Option> options
    ) {
        public static OptionRegister from(Product product) {
            List<Option> registeredOptions = mappingOptionTypes(product.getOptions());
            return OptionRegister.builder()
                    .productId(product.getId())
                    .options(registeredOptions)
                    .build();
        }

        private static List<Option> mappingOptionTypes(List<ProductOption> productOptions) {
            return productOptions.stream().map(Option::from).toList();
        }
    }

    @Builder
    public record Option (
            Long optionTypeId,
            String optionTypeName,
            Integer priority
    ) {
        public static Option from(ProductOption productOption) {
            return Option.builder()
                    .optionTypeId(productOption.getOptionType().getId())
                    .optionTypeName(productOption.getOptionType().getName())
                    .priority(productOption.getPriority())
                    .build();
        }
    }

    @Builder
    public record AddVariant(
            Long productId,
            List<VariantDetail> variants
    ) {
        public static AddVariant of(Long productId, List<ProductVariant> variants) {
            List<VariantDetail> variantDetails = mappingVariants(variants);
            return AddVariant.builder()
                    .productId(productId)
                    .variants(variantDetails)
                    .build();
        }

        private static List<VariantDetail> mappingVariants(List<ProductVariant> variants) {
            return variants.stream().map(VariantDetail::from).toList();
        }
    }

    @Builder(toBuilder = true)
    public record VariantDetail (
            Long variantId,
            String sku,
            List<Long> optionValueIds,
            Long originalPrice,
            Long discountedPrice,
            Integer discountRate,
            Integer stockQuantity
    ) {
        public static VariantDetail from (ProductVariant variant) {
            List<Long> variantOptions = mappingOptionValueIds(variant.getProductVariantOptions());
            return VariantDetail.builder()
                    .variantId(variant.getId())
                    .sku(variant.getSku())
                    .optionValueIds(variantOptions)
                    .originalPrice(variant.getOriginalPrice())
                    .discountedPrice(variant.getPrice())
                    .discountRate(variant.getDiscountRate())
                    .stockQuantity(variant.getStockQuantity())
                    .build();
        }

        private static List<Long> mappingOptionValueIds(List<ProductVariantOption> variantOptions) {
            return variantOptions.stream().map(option -> option.getOptionValue().getId()).toList();
        }
    }

    @Builder
    public record AddImage (
            Long productId,
            List<ImageDetail> images
    ) {
        public static AddImage of(Long productId, List<ProductImage> images) {
            List<ImageDetail> imageDetails = mappingImageDetail(images);
            return AddImage.builder()
                    .productId(productId)
                    .images(imageDetails)
                    .build();
        }

        private static List<ImageDetail> mappingImageDetail(List<ProductImage> images) {
            return images.stream().map(ImageDetail::from).toList();
        }
    }

    @Builder
    public record ImageDetail (
            Long imageId,
            String imagePath,
            Integer sortOrder,
            boolean isThumbnail
    ) {
        public static ImageDetail from(ProductImage image) {
            return ImageDetail.builder()
                    .imageId(image.getId())
                    .imagePath(image.getImageUrl())
                    .sortOrder(image.getSortOrder())
                    .isThumbnail(image.isThumbnail())
                    .build();
        }
    }

    @Builder
    public record AddDescriptionImage (
            Long productId,
            List<DescriptionImageDetail> images
    ) {
        public static AddDescriptionImage of(Long productId, List<ProductDescriptionImage> images) {
            List<DescriptionImageDetail> descriptionImageDetails = mappingDescriptionImages(images);
            return AddDescriptionImage.builder()
                    .productId(productId)
                    .images(descriptionImageDetails)
                    .build();
        }

        private static List<DescriptionImageDetail> mappingDescriptionImages(List<ProductDescriptionImage> images) {
            return images.stream().map(DescriptionImageDetail::from).toList();
        }
    }

    @Builder
    public record DescriptionImageDetail (
            Long imageId,
            String imagePath,
            Integer sortOrder
    ) {
        public static DescriptionImageDetail from(ProductDescriptionImage image) {
            return DescriptionImageDetail.builder()
                    .imageId(image.getId())
                    .imagePath(image.getImageUrl())
                    .sortOrder(image.getSortOrder())
                    .build();
        }
    }

    @Builder
    public record Publish (
            Long productId,
            ProductStatus status,
            LocalDateTime publishedAt
    ) {
        public static Publish from(Product product) {
            return Publish.builder()
                    .productId(product.getId())
                    .status(product.getStatus())
                    .publishedAt(product.getPublishedAt())
                    .build();
        }
    }

    @Builder
    public record Close (
            Long productId,
            ProductStatus status,
            LocalDateTime saleStoppedAt
    ) {
        public static Close from(Product product) {
            return Close.builder()
                    .productId(product.getId())
                    .status(product.getStatus())
                    .saleStoppedAt(product.getSaleStoppedAt())
                    .build();
        }
    }

    @Builder
    public record Update (
            Long productId,
            String name,
            String description,
            Long categoryId
    ) {
        public static Update from(Product product) {
            return Update.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .categoryId(product.getCategory().getId())
                    .build();
        }
    }
}
