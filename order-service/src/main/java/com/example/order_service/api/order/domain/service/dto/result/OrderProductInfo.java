package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderProductInfo {
    private Long productId;
    private Long productVariantId;
    private String sku;
    private String productName;
    private Long originalPrice;
    private Integer discountRate;
    private Long discountAmount;
    private Long discountedPrice;
    private String thumbnail;
    private List<ProductOption> productOption;

    @Getter
    @Builder
    public static class ProductOption {
        private String optionTypeName;
        private String optionValueName;
    }
}
