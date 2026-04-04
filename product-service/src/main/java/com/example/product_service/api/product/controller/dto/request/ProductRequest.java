package com.example.product_service.api.product.controller.dto.request;

import com.example.product_service.api.product.controller.validation.annotation.UniqueOptionTypes;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand.VariantDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public class ProductRequest {

    @Builder
    public record CreateRequest(
            @NotBlank(message = "상품 이름은 필수 입니다")
            String name,
            @NotNull(message = "카테고리 id는 필수 입니다")
            Long categoryId,
            String description
    )
    {
        public ProductCreateCommand toCommand() {
            return ProductCreateCommand.builder()
                    .name(this.name())
                    .categoryId(this.categoryId())
                    .description(this.description())
                    .build();
        }
    }

    @Builder
    public record OptionRegisterRequest(
            @Valid
            @NotNull(message = "옵션 리스트는 필수 입니다")
            @Size(max = 3, message = "옵션은 최대 3개까지만 설정 가능합니다")
            @UniqueOptionTypes(message = "중복된 옵션 종류(optionTypeId)가 포함되어 있습니다")
            List<ProductOptionRequest> options
    ) {}

    @Builder(toBuilder = true)
    public record ProductOptionRequest (
            @NotNull(message = "옵션 타입 Id는 필수 입니다")
            Long optionTypeId,
            @NotNull(message = "옵션 우선순위는 필수 입니다")
            @Min(value = 1, message = "옵션 우선순위는 1이상 이여야 합니다")
            Integer priority
    ) {}

    @Builder
    public record AddVariantRequest(
            @Valid
            @NotEmpty(message = "상품 변형 리스트는 필수입니다")
            List<VariantRequest> variants
    ) {
        public ProductVariantsCreateCommand toCommand(Long productId) {
            List<VariantDetail> variantDetails = mappingVariantDetails(variants);
            return ProductVariantsCreateCommand.builder()
                    .productId(productId)
                    .variants(variantDetails)
                    .build();
        }

        private List<VariantDetail> mappingVariantDetails(List<VariantRequest> variants) {
            return variants.stream().map(
                    v -> VariantDetail.builder()
                            .originalPrice(v.originalPrice())
                            .discountRate(v.discountRate())
                            .stockQuantity(v.stockQuantity())
                            .optionValueIds(v.optionValueIds())
                            .build()).toList();
        }
    }

    @Builder(toBuilder = true)
    public record VariantRequest(
            @NotNull(message = "가격은 필수 입니다")
            @Min(value = 100, message = "가격은 100 이상이여야 합니다")
            Long originalPrice,
            @NotNull(message = "할인율은 필수 입니다")
            @Min(value = 0, message = "할인율은 0 이상이여야 합니다")
            @Max(value = 100, message = "할인율은 100 이하여야 합니다")
            Integer discountRate,
            @NotNull(message = "재고 수량은 필수 입니다")
            @Min(value = 1, message = "재고 수량은 1 이상이여야 합니다")
            Integer stockQuantity,
            @NotNull(message = "상품 변형 옵션은 필수 입니다")
            @UniqueElements(message = "중복된 옵션 종류가 포함되어 있습니다")
            List<Long> optionValueIds
    ) { }

    @Builder
    public record AddImageRequest (
        @Valid
        @NotEmpty(message = "최소 1장의 이미지를 등록해야 합니다")
        List<ImageRequest> images
    ) { }

    @Builder(toBuilder = true)
    public record ImageRequest (
            @NotBlank(message = "이미지 경로는 필수 입니다")
            @Pattern(
                    regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                    message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
            )
            String imagePath,
            @NotNull(message = "썸네일 여부는 필수 입니다")
            Boolean isThumbnail,
            @NotNull(message = "정렬 순서는 필수 입니다")
            @Min(value = 1, message = "정렬 순서는 1 이상이여야 합니다")
            Integer sortOrder
    ) {}

    @Builder
    public record AddDescriptionImageRequest (
            @Valid
            @NotEmpty(message = "최소 1장의 이미지를 등록해야 합니다")
            List<DescriptionImageRequest> images
    ) { }

    @Builder(toBuilder = true)
    public record DescriptionImageRequest (
            @NotBlank(message = "이미지 경로는 필수 입니다")
            @Pattern(
                    regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                    message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
            )
            String imagePath,
            @NotNull(message = "정렬 순서는 필수 입니다")
            @Min(value = 1, message = "정렬 순서는 1 이상이여야 합니다")
            Integer sortOrder
    ) {}
}
