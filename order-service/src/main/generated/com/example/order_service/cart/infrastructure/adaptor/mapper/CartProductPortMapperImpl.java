package com.example.order_service.cart.infrastructure.adaptor.mapper;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-27T02:34:13+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CartProductPortMapperImpl implements CartProductPortMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public CartProductPortMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public CartProductResult toCartProductResult(ProductResponse response) {
        if ( response == null ) {
            return null;
        }

        CartProductResult.CartProductResultBuilder cartProductResult = CartProductResult.builder();

        cartProductResult.products( productDetailListToCartProductDetailList( response.products() ) );

        return cartProductResult.build();
    }

    @Override
    public CartProductResult.CartProductDetail toCartProductDetail(ProductResponse.ProductDetail product) {
        if ( product == null ) {
            return null;
        }

        CartProductResult.CartProductDetail.CartProductDetailBuilder cartProductDetail = CartProductResult.CartProductDetail.builder();

        cartProductDetail.originalPrice( moneyMapper.toMoney( productUnitPriceOriginalPrice( product ) ) );
        cartProductDetail.discountAmount( moneyMapper.toMoney( productUnitPriceDiscountAmount( product ) ) );
        cartProductDetail.discountedPrice( moneyMapper.toMoney( productUnitPriceDiscountedPrice( product ) ) );
        cartProductDetail.status( toProductStatus( product.status() ) );
        cartProductDetail.productId( product.productId() );
        cartProductDetail.productVariantId( product.productVariantId() );
        cartProductDetail.stock( product.stock() );
        cartProductDetail.sku( product.sku() );
        cartProductDetail.productName( product.productName() );
        cartProductDetail.thumbnail( product.thumbnail() );
        cartProductDetail.options( productOptionListToProductOptionList( product.options() ) );

        return cartProductDetail.build();
    }

    @Override
    public CartProductResult.ProductOption toOption(ProductResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        CartProductResult.ProductOption.ProductOptionBuilder productOption = CartProductResult.ProductOption.builder();

        productOption.optionTypeName( option.optionTypeName() );
        productOption.optionValueName( option.optionValueName() );

        return productOption.build();
    }

    protected List<CartProductResult.CartProductDetail> productDetailListToCartProductDetailList(List<ProductResponse.ProductDetail> list) {
        if ( list == null ) {
            return new ArrayList<CartProductResult.CartProductDetail>();
        }

        List<CartProductResult.CartProductDetail> list1 = new ArrayList<CartProductResult.CartProductDetail>( list.size() );
        for ( ProductResponse.ProductDetail productDetail : list ) {
            list1.add( toCartProductDetail( productDetail ) );
        }

        return list1;
    }

    private Long productUnitPriceOriginalPrice(ProductResponse.ProductDetail productDetail) {
        ProductResponse.UnitPrice unitPrice = productDetail.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.originalPrice();
    }

    private Long productUnitPriceDiscountAmount(ProductResponse.ProductDetail productDetail) {
        ProductResponse.UnitPrice unitPrice = productDetail.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountAmount();
    }

    private Long productUnitPriceDiscountedPrice(ProductResponse.ProductDetail productDetail) {
        ProductResponse.UnitPrice unitPrice = productDetail.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountedPrice();
    }

    protected List<CartProductResult.ProductOption> productOptionListToProductOptionList(List<ProductResponse.ProductOption> list) {
        if ( list == null ) {
            return new ArrayList<CartProductResult.ProductOption>();
        }

        List<CartProductResult.ProductOption> list1 = new ArrayList<CartProductResult.ProductOption>( list.size() );
        for ( ProductResponse.ProductOption productOption : list ) {
            list1.add( toOption( productOption ) );
        }

        return list1;
    }
}
