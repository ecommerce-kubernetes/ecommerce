package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductStatusResult {
    private Long productId;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime saleStoppedAt;

    public static ProductStatusResult publish(Product product) {
        return ProductStatusResult.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .publishedAt(product.getPublishedAt())
                .build();
    }

    public static ProductStatusResult closed(Product product) {
        return ProductStatusResult.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .saleStoppedAt(product.getSaleStoppedAt())
                .build();
    }
}
