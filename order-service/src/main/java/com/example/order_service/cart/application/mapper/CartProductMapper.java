package com.example.order_service.cart.application.mapper;

import com.example.order_service.cart.application.external.dto.result.CartProductResult;
import com.example.order_service.cart.domain.model.vo.ProductStatus;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CartProductMapper {


    CartProductResult.Option toOption(ProductClientResponse.ProductOption option);

    default ProductStatus translateStatus(String status) {
        if (status == null) {
            return ProductStatus.UNAVAILABLE;
        }

        if (status.equals("ON_SALE")) {
            return ProductStatus.AVAILABLE;
        } else {
            return ProductStatus.UNAVAILABLE;
        }
    }
}
