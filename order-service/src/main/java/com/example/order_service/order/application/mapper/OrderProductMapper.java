package com.example.order_service.order.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.domain.model.vo.ProductStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderProductMapper {

    @Mapping(source = "stockQuantity", target = "stock")
    @Mapping(source = "unitPrice.originalPrice", target = "originalPrice")
    @Mapping(source = "unitPrice.discountRate", target = "discountRate")
    @Mapping(source = "unitPrice.discountAmount", target = "discountAmount")
    @Mapping(source = "unitPrice.discountedPrice", target = "discountedPrice")
    @Mapping(source = "itemOptions", target = "options")
    OrderProductResult.Info toResult(ProductClientResponse.Product product);

    OrderProductResult.Option toOption(ProductClientResponse.ProductOption option);

    default ProductStatus translateStatus(String status) {
        if (status == null) {
            return ProductStatus.UNORDERABLE;
        }

        if (status.equals("ON_SALE")) {
            return ProductStatus.ORDERABLE;
        } else {
            return ProductStatus.UNORDERABLE;
        }
    }
}
