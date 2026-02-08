package com.example.order_service.api.cart.domain.service.dto.result;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartProductInfo {
    private Long productId;
    private Long productVariantId;
    private ProductStatus status;
    private String productName;
    private String thumbnail;
    private Long originalPrice;
    private Integer discountRate;
    private Long discountAmount;
    private Long discountedPrice;
    private List<ProductOption> productOption;


    @Getter
    @Builder
    public static class ProductOption {
        private String optionTypeName;
        private String optionValueName;

        public static ProductOption of(String optionTypeName, String optionValueName) {
            return ProductOption.builder()
                    .optionTypeName(optionTypeName)
                    .optionValueName(optionValueName)
                    .build();
        }
    }
}
