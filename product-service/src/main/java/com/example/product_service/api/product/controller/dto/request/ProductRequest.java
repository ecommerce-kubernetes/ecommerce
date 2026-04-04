package com.example.product_service.api.product.controller.dto.request;

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
            @NotNull(message = "옵션 id 리스트는 필수 입니다")
            @Size(max = 3, message = "옵션은 최대 3개까지만 설정 가능합니다")
            @UniqueElements(message = "중복된 옵션 종류가 포함되어 있습니다")
            List<Long> optionTypeIds
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
}
