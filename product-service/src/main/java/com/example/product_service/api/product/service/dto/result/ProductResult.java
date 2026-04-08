package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductOption;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.model.ProductVariantOption;
import lombok.Builder;

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
}
