package com.example.order_service.cart.application.external.mapper;

import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-11T01:45:17+0900",
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
    public CartProductListResult toResult(ProductClientResponse.ProductList productList) {
        if ( productList == null ) {
            return null;
        }

        CartProductListResult.CartProductListResultBuilder cartProductListResult = CartProductListResult.builder();

        cartProductListResult.products( productListToCartProductResultList( productList.products() ) );

        return cartProductListResult.build();
    }

    @Override
    public CartProductResult toProduct(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        CartProductResult.CartProductResultBuilder cartProductResult = CartProductResult.builder();

        cartProductResult.originalPrice( moneyMapper.toMoney( productUnitPriceOriginalPrice( product ) ) );
        cartProductResult.discountRate( productUnitPriceDiscountRate( product ) );
        cartProductResult.discountAmount( moneyMapper.toMoney( productUnitPriceDiscountAmount( product ) ) );
        cartProductResult.discountedPrice( moneyMapper.toMoney( productUnitPriceDiscountedPrice( product ) ) );
        cartProductResult.productId( product.productId() );
        cartProductResult.productVariantId( product.productVariantId() );
        cartProductResult.status( toCartStatus( product.status() ) );
        cartProductResult.stock( product.stock() );
        cartProductResult.sku( product.sku() );
        cartProductResult.productName( product.productName() );
        cartProductResult.thumbnail( product.thumbnail() );
        cartProductResult.options( productOptionListToProductOptionList( product.options() ) );

        return cartProductResult.build();
    }

    protected List<CartProductResult> productListToCartProductResultList(List<ProductClientResponse.Product> list) {
        if ( list == null ) {
            return null;
        }

        List<CartProductResult> list1 = new ArrayList<CartProductResult>( list.size() );
        for ( ProductClientResponse.Product product : list ) {
            list1.add( toProduct( product ) );
        }

        return list1;
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

    protected CartProductResult.ProductOption productOptionToProductOption(ProductClientResponse.ProductOption productOption) {
        if ( productOption == null ) {
            return null;
        }

        CartProductResult.ProductOption.ProductOptionBuilder productOption1 = CartProductResult.ProductOption.builder();

        productOption1.optionTypeName( productOption.optionTypeName() );
        productOption1.optionValueName( productOption.optionValueName() );

        return productOption1.build();
    }

    protected List<CartProductResult.ProductOption> productOptionListToProductOptionList(List<ProductClientResponse.ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<CartProductResult.ProductOption> list1 = new ArrayList<CartProductResult.ProductOption>( list.size() );
        for ( ProductClientResponse.ProductOption productOption : list ) {
            list1.add( productOptionToProductOption( productOption ) );
        }

        return list1;
    }
}
