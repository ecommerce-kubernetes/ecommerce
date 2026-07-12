package com.example.order_service.cart.application.external.mapper;


import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.common.mapper.MoneyMapper;

import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CartProductMapper {

    CartProductListResult toResult(ProductClientResponse.ProductList productList);

    @Mapping(source = "unitPrice.originalPrice", target = "originalPrice")
    @Mapping(source = "unitPrice.discountRate", target = "discountRate")
    @Mapping(source = "unitPrice.discountAmount", target = "discountAmount")
    @Mapping(source = "unitPrice.discountedPrice", target = "discountedPrice")
    CartProductResult toProduct(ProductClientResponse.Product product);

    default CartProductStatus toCartStatus(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "ON_SALE" -> CartProductStatus.ON_SALE;
            case "PREPARING" -> CartProductStatus.PREPARING;
            case "STOP_SALE" -> CartProductStatus.STOP_SALE;
            case "DELETED" -> CartProductStatus.DELETED;
            default -> CartProductStatus.UNKNOWN;
        };
    }
}
