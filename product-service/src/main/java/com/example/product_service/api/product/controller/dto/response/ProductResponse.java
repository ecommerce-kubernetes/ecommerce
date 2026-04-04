package com.example.product_service.api.product.controller.dto.response;

import com.example.product_service.api.product.service.dto.result.ProductCreateResult;
import com.example.product_service.api.product.service.dto.result.ProductOptionResponse;
import com.example.product_service.api.product.service.dto.result.ProductOptionResponse.OptionDto;
import lombok.Builder;

import java.util.List;

public class ProductResponse {

    @Builder
    public record CreateResponse (
            Long productId
    ) {
        public static CreateResponse from(ProductCreateResult result) {
            return CreateResponse.builder()
                    .productId(result.getProductId())
                    .build();
        }
    }

    @Builder
    public record OptionRegisterResponse (
            Long productId,
            List<OptionInfo> options
    ) {
        public static OptionRegisterResponse from(ProductOptionResponse result) {
            List<OptionInfo> optionInfos = mappingOptionInfo(result.getOptions());
            return OptionRegisterResponse.builder()
                    .productId(result.getProductId())
                    .options(optionInfos)
                    .build();
        }

        private static List<OptionInfo> mappingOptionInfo(List<OptionDto> options) {
            return options.stream().map(OptionInfo::from).toList();
        }
    }

    @Builder
    public record OptionInfo (
            Long optionTypeId,
            String optionTypeName,
            Integer priority
    ) {
        public static OptionInfo from(OptionDto option) {
           return OptionInfo.builder()
                   .optionTypeId(option.getOptionTypeId())
                   .optionTypeName(option.getOptionTypeName())
                   .priority(option.getPriority())
                   .build();
        }
    }
}
