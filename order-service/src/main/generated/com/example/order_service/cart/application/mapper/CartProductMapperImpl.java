package com.example.order_service.cart.application.mapper;

import com.example.order_service.cart.application.external.dto.result.CartProductResult;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-04T20:09:32+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CartProductMapperImpl implements CartProductMapper {

    @Override
    public CartProductResult.ProductOption toOption(ProductClientResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        CartProductResult.ProductOption.ProductOptionBuilder productOption = CartProductResult.ProductOption.builder();

        productOption.optionTypeName( option.optionTypeName() );
        productOption.optionValueName( option.optionValueName() );

        return productOption.build();
    }
}
