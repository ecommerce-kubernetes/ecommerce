package com.example.order_service.order.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.result.OrderSheetProductResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-25T20:24:37+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderSheetProductMapperImpl implements OrderSheetProductMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public OrderSheetProductMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public OrderSheetProductResult.ProductList toResult(ProductClientResponse.ProductList productList) {
        if ( productList == null ) {
            return null;
        }

        OrderSheetProductResult.ProductList.ProductListBuilder productList1 = OrderSheetProductResult.ProductList.builder();

        productList1.products( productListToInfoList( productList.products() ) );

        return productList1.build();
    }

    @Override
    public OrderSheetProductResult.Info toProduct(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        OrderSheetProductResult.Info.InfoBuilder info = OrderSheetProductResult.Info.builder();

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
    public OrderSheetProductResult.Option toOption(ProductClientResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        OrderSheetProductResult.Option.OptionBuilder option1 = OrderSheetProductResult.Option.builder();

        option1.optionTypeName( option.optionTypeName() );
        option1.optionValueName( option.optionValueName() );

        return option1.build();
    }

    protected List<OrderSheetProductResult.Info> productListToInfoList(List<ProductClientResponse.Product> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetProductResult.Info> list1 = new ArrayList<OrderSheetProductResult.Info>( list.size() );
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

    protected List<OrderSheetProductResult.Option> productOptionListToOptionList(List<ProductClientResponse.ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetProductResult.Option> list1 = new ArrayList<OrderSheetProductResult.Option>( list.size() );
        for ( ProductClientResponse.ProductOption productOption : list ) {
            list1.add( toOption( productOption ) );
        }

        return list1;
    }
}
