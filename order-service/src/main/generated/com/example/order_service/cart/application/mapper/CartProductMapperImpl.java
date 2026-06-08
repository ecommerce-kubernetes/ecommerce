package com.example.order_service.cart.application.mapper;

import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-09T01:43:24+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CartProductMapperImpl implements CartProductMapper {

    @Override
    public CartProductResult.Option toOption(ProductClientResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        CartProductResult.Option.OptionBuilder option1 = CartProductResult.Option.builder();

        option1.optionTypeName( option.optionTypeName() );
        option1.optionValueName( option.optionValueName() );

        return option1.build();
    }
}
