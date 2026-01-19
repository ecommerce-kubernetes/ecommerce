package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductStatusResponse {
    private Long productId;
    private String status;
    private String publishedAt;
    private String saleStoppedAt;

    public static ProductStatusResponse publish(Product product) {
        return ProductStatusResponse.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .publishedAt(product.getPublishedAt().toString())
                .build();
    }

    public static ProductStatusResponse closed(Product product) {
        return ProductStatusResponse.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .saleStoppedAt(product.getSaleStoppedAt().toString())
                .build();
    }
}
