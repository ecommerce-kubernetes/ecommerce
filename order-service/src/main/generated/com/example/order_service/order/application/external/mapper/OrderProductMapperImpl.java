package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-24T01:01:14+0900",
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
    public OrderProductResult toResult(ProductClientResponse.ProductList productList) {
        if ( productList == null ) {
            return null;
        }

        OrderProductResult.OrderProductResultBuilder orderProductResult = OrderProductResult.builder();

        orderProductResult.products( productListToOrderProductDetailList( productList.products() ) );

        return orderProductResult.build();
    }

    @Override
    public OrderProductResult.OrderProductDetail toProduct(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        OrderProductResult.OrderProductDetail.OrderProductDetailBuilder orderProductDetail = OrderProductResult.OrderProductDetail.builder();

        orderProductDetail.productSnapshot( toProductSnapshot( product ) );
        orderProductDetail.priceSnapshot( toPriceSnapshot( product.unitPrice() ) );
        orderProductDetail.options( productOptionListToProductOptionSnapshotList( product.options() ) );
        orderProductDetail.status( toOrderStatus( product.status() ) );
        orderProductDetail.stock( product.stock() );

        return orderProductDetail.build();
    }

    @Override
    public ProductSnapshot toProductSnapshot(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        ProductSnapshot.ProductSnapshotBuilder productSnapshot = ProductSnapshot.reconstitute();

        productSnapshot.productId( product.productId() );
        productSnapshot.productVariantId( product.productVariantId() );
        productSnapshot.sku( product.sku() );
        productSnapshot.productName( product.productName() );
        productSnapshot.thumbnail( product.thumbnail() );

        return productSnapshot.build();
    }

    @Override
    public ProductPriceSnapshot toPriceSnapshot(ProductClientResponse.UnitPrice unitPrice) {
        if ( unitPrice == null ) {
            return null;
        }

        ProductPriceSnapshot.ProductPriceSnapshotBuilder productPriceSnapshot = ProductPriceSnapshot.reconstitute();

        productPriceSnapshot.originalPrice( moneyMapper.toMoney( unitPrice.originalPrice() ) );
        productPriceSnapshot.discountRate( unitPrice.discountRate() );
        productPriceSnapshot.discountAmount( moneyMapper.toMoney( unitPrice.discountAmount() ) );
        productPriceSnapshot.discountedPrice( moneyMapper.toMoney( unitPrice.discountedPrice() ) );

        return productPriceSnapshot.build();
    }

    @Override
    public ProductOptionSnapshot toOptionSnapshot(ProductClientResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        ProductOptionSnapshot.ProductOptionSnapshotBuilder productOptionSnapshot = ProductOptionSnapshot.reconstitute();

        productOptionSnapshot.optionTypeName( option.optionTypeName() );
        productOptionSnapshot.optionValueName( option.optionValueName() );

        return productOptionSnapshot.build();
    }

    protected List<OrderProductResult.OrderProductDetail> productListToOrderProductDetailList(List<ProductClientResponse.Product> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderProductResult.OrderProductDetail> list1 = new ArrayList<OrderProductResult.OrderProductDetail>( list.size() );
        for ( ProductClientResponse.Product product : list ) {
            list1.add( toProduct( product ) );
        }

        return list1;
    }

    protected List<ProductOptionSnapshot> productOptionListToProductOptionSnapshotList(List<ProductClientResponse.ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductOptionSnapshot> list1 = new ArrayList<ProductOptionSnapshot>( list.size() );
        for ( ProductClientResponse.ProductOption productOption : list ) {
            list1.add( toOptionSnapshot( productOption ) );
        }

        return list1;
    }
}
