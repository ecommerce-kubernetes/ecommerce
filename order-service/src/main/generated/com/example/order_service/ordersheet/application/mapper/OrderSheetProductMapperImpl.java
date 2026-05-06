package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-07T01:27:01+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderSheetProductMapperImpl implements OrderSheetProductMapper {

    @Override
    public OrderSheetProductResult.Info toResult(ProductClientResponse.Product product) {
        if ( product == null ) {
            return null;
        }

        OrderSheetProductResult.Info.InfoBuilder info = OrderSheetProductResult.Info.builder();

        info.stock( product.stockQuantity() );
        info.originalPrice( productUnitPriceOriginalPrice( product ) );
        info.discountRate( productUnitPriceDiscountRate( product ) );
        info.discountAmount( productUnitPriceDiscountAmount( product ) );
        info.discountedPrice( productUnitPriceDiscountedPrice( product ) );
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
    public OrderSheetProductResult.Option toOption(ProductClientResponse.ProductOption option) {
        if ( option == null ) {
            return null;
        }

        OrderSheetProductResult.Option.OptionBuilder option1 = OrderSheetProductResult.Option.builder();

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
