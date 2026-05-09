package com.example.order_service.cart.application.mapper;

import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-09T19:29:53+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CartProductMapperImpl implements CartProductMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public CartProductMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public CartProductResult.Info toResult(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        CartProductResult.Info.InfoBuilder info = CartProductResult.Info.builder();

        info.stock( product.stockQuantity() );
        info.originalPrice( moneyMapper.toMoney( productUnitPriceOriginalPrice( product ) ) );
        info.discountRate( productUnitPriceDiscountRate( product ) );
        info.discountAmount( moneyMapper.toMoney( productUnitPriceDiscountAmount( product ) ) );
        info.discountedPrice( moneyMapper.toMoney( productUnitPriceDiscountedPrice( product ) ) );
        info.options( productOptionListToOptionList( product.itemOptions() ) );
        info.productId( product.productId() );
        info.productVariantId( product.productVariantId() );
        info.status( translateStatus( product.status() ) );
        info.sku( product.sku() );
        info.productName( product.productName() );
        info.thumbnail( product.thumbnail() );

        return info.build();
    }

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

    private Long productUnitPriceOriginalPrice(ProductClientResponse.Product product) {
        ProductClientResponse.UnitPrice unitPrice = product.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.originalPrice();
    }

    private Integer productUnitPriceDiscountRate(ProductClientResponse.Product product) {
        ProductClientResponse.UnitPrice unitPrice = product.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountRate();
    }

    private Long productUnitPriceDiscountAmount(ProductClientResponse.Product product) {
        ProductClientResponse.UnitPrice unitPrice = product.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountAmount();
    }

    private Long productUnitPriceDiscountedPrice(ProductClientResponse.Product product) {
        ProductClientResponse.UnitPrice unitPrice = product.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountedPrice();
    }

    protected List<CartProductResult.Option> productOptionListToOptionList(List<ProductClientResponse.ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<CartProductResult.Option> list1 = new ArrayList<CartProductResult.Option>( list.size() );
        for ( ProductClientResponse.ProductOption productOption : list ) {
            list1.add( toOption( productOption ) );
        }

        return list1;
    }
}
