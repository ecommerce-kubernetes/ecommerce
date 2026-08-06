package com.example.order_service.cart.infrastructure.adaptor.mapper;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.exception.CartProductPortErrorCode;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {MoneyMapper.class}, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface CartProductPortMapper {

    CartProductResult toCartProductResult(ProductResponse response);

    @Mapping(target = "originalPrice", source = "unitPrice.originalPrice")
    @Mapping(target = "discountAmount", source = "unitPrice.discountAmount")
    @Mapping(target = "discountRate", source = "unitPrice.discountRate")
    @Mapping(target = "discountedPrice", source = "unitPrice.discountedPrice")
    @Mapping(target = "status", source = "status")
    CartProductResult.CartProductDetail toCartProductDetail(ProductResponse.ProductDetail product);

    CartProductResult.ProductOption toOption(ProductResponse.ProductOption option);

    default CartProductStatus toProductStatus(String productStatus) {
        return switch (productStatus) {
            case "ON_SALE" -> CartProductStatus.ON_SALE;
            case "STOP_SALE" -> CartProductStatus.STOP_SALE;
            case "DELETED" -> CartProductStatus.DELETED;
            case "PREPARING" -> CartProductStatus.PREPARING;
            case null, default -> throw new PortException(
                    CartProductPortErrorCode.PRODUCT_CLIENT_ERROR,
                    "UNSUPPORTED_STATUS",
                    "처리할 수 없는 상품 상태입니다"
            );
        };
    }
}
