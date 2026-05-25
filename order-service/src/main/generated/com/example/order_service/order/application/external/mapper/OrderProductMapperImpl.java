package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-25T22:04:19+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderProductMapperImpl implements OrderProductMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public OrderProductMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public OrderProductResult.ProductList toResult(ProductClientResponse.ProductList productList) {
        if ( productList == null ) {
            return null;
        }

        OrderProductResult.ProductList.ProductListBuilder productList1 = OrderProductResult.ProductList.builder();

        productList1.products( productListToInfoList( productList.products() ) );

        return productList1.build();
    }

    @Override
    public OrderProductResult.Info toProduct(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        OrderProductResult.Info.InfoBuilder info = OrderProductResult.Info.builder();

        info.originalPrice( moneyMapper.toMoney( productUnitPriceOriginalPrice( product ) ) );
        info.discountRate( productUnitPriceDiscountRate( product ) );
        info.discountAmount( moneyMapper.toMoney( productUnitPriceDiscountAmount( product ) ) );
        info.discountedPrice( moneyMapper.toMoney( productUnitPriceDiscountedPrice( product ) ) );
        info.productId( product.productId() );
        info.productVariantId( product.productVariantId() );
        info.sku( product.sku() );
        info.productName( product.productName() );
        info.thumbnail( product.thumbnail() );
        info.options( productOptionListToOptionList( product.options() ) );

        return info.build();
    }

    @Override
    public OrderProductResult.Option toOption(ProductClientResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        OrderProductResult.Option.OptionBuilder option1 = OrderProductResult.Option.builder();

        option1.optionTypeName( option.optionTypeName() );
        option1.optionValueName( option.optionValueName() );

        return option1.build();
    }

    protected List<OrderProductResult.Info> productListToInfoList(List<ProductClientResponse.Product> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderProductResult.Info> list1 = new ArrayList<OrderProductResult.Info>( list.size() );
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

    protected List<OrderProductResult.Option> productOptionListToOptionList(List<ProductClientResponse.ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderProductResult.Option> list1 = new ArrayList<OrderProductResult.Option>( list.size() );
        for ( ProductClientResponse.ProductOption productOption : list ) {
            list1.add( toOption( productOption ) );
        }

        return list1;
    }
}
