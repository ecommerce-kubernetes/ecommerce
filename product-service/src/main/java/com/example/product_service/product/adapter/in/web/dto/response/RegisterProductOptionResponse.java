package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterProductOptionResponse(
        Long productId,
        List<ProductOptionInfoResponse> options
) {
    public static RegisterProductOptionResponse from(ProductResult.OptionRegister result) {
        List<ProductOptionInfoResponse> optionInfos = mappingOptionInfo(result.options());
        return RegisterProductOptionResponse.builder()
                .productId(result.productId())
                .options(optionInfos)
                .build();
    }

    private static List<ProductOptionInfoResponse> mappingOptionInfo(List<ProductResult.Option> options) {
        return options.stream().map(ProductOptionInfoResponse::from).toList();
    }
}
