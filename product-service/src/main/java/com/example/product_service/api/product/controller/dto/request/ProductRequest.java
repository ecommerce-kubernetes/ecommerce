package com.example.product_service.api.product.controller.dto.request;

import com.example.product_service.api.product.service.dto.command.ProductCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class ProductRequest {

    @Builder
    public record Create(
            @NotBlank(message = "상품 이름은 필수 입니다")
            String name,
            @NotNull(message = "카테고리 id는 필수 입니다")
            Long categoryId,
            String description
    )
    {
        public ProductCommand.Create toCommand() {
            return ProductCommand.Create.builder()
                    .name(name)
                    .categoryId(categoryId)
                    .description(description)
                    .build();
        }
    }

    @Builder
    public record OptionRegister(
            @Valid
            @NotNull(message = "옵션 리스트는 필수 입니다")
            @Size(max = 3, message = "옵션은 최대 3개까지만 설정 가능합니다")
            @UniqueElements(message = "옵션 ID는 중복될 수 없습니다")
            List<Long> optionTypeIds
    ) {
        public ProductCommand.OptionRegister toCommand(Long productId) {
            return ProductCommand.OptionRegister.builder()
                    .productId(productId)
                    .optionTypeIds(optionTypeIds)
                    .build();
        }
    }

    @Builder
    public record AddVariant(
            @Valid
            @NotEmpty(message = "상품 변형 리스트는 필수입니다")
            List<VariantDetail> variants
    ) {
        public ProductCommand.AddVariant toCommand(Long productId) {
            List<ProductCommand.VariantDetail> variantDetails = mappingVariantDetails(variants);
            return ProductCommand.AddVariant.builder()
                    .productId(productId)
                    .variants(variantDetails)
                    .build();
        }

        private List<ProductCommand.VariantDetail> mappingVariantDetails(List<ProductRequest.VariantDetail> variants) {
            return variants.stream().map(
                    v -> ProductCommand.VariantDetail.builder()
                            .originalPrice(v.originalPrice())
                            .discountRate(v.discountRate())
                            .stockQuantity(v.stockQuantity())
                            .optionValueIds(v.optionValueIds())
                            .build()).toList();
        }
    }

    @Builder(toBuilder = true)
    public record VariantDetail(
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
    public record AddImage(
        @NotEmpty(message = "최소 1장의 이미지를 등록해야 합니다")
        List<@Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다") String> images
    ) {
        public ProductCommand.AddImage toCommand(Long productId) {
            return ProductCommand.AddImage.builder()
                    .productId(productId)
                    .images(images)
                    .build();
        }
    }

    @Builder
    public record AddDescriptionImage(
            @NotEmpty(message = "최소 1장의 이미지를 등록해야 합니다")
            List<@Pattern(
                    regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                    message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다") String> images
    ) {
        public ProductCommand.AddDescriptionImage toCommand(Long productId) {
            return ProductCommand.AddDescriptionImage.builder()
                    .productId(productId)
                    .images(images)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Update(
            @NotBlank(message = "상품 이름은 필수 입니다")
            String name,
            @NotNull(message = "카테고리 id는 필수 입니다")
            Long categoryId,
            String description
    ) {
        public ProductCommand.Update toCommand(Long productId) {
            return ProductCommand.Update.builder()
                    .productId(productId)
                    .name(name)
                    .categoryId(categoryId)
                    .description(description)
                    .build();
        }
    }

    @Builder
    public static class Search {
        private Integer page = 1;
        private Integer size = 20;
        private String sort = "latest";
        @Min(value = 1, message = "카테고리 Id는 0 또는 음수일 수 없습니다")
        private Long categoryId;
        private String name;
        @Min(value = 0, message = "평점은 음수일 수 없습니다")
        @Max(value = 5, message = "최대 평점은 5점입니다")
        private Integer rating;

        public ProductCommand.Search toCommand() {
            int validPage = (this.page != null && this.page > 0) ? this.page - 1 : 0;
            int validSize = (this.size != null && this.size > 0) ? Math.min(this.size, 100) : 20;

            return ProductCommand.Search.builder()
                    .categoryId(categoryId)
                    .name(name)
                    .rating(rating)
                    .pageable(PageRequest.of(validPage,validSize))
                    .sort(sort)
                    .build();
        }
    }
}
