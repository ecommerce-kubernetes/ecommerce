package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-16T03:24:38+0900",
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
    public OrderSheetProductResult.InfoDeprecated toResult(ProductClientResponse.ProductDeprecated productDeprecated) {
        if ( productDeprecated == null ) {
            return null;
        }

        OrderSheetProductResult.InfoDeprecated.InfoDeprecatedBuilder infoDeprecated = OrderSheetProductResult.InfoDeprecated.builder();

        infoDeprecated.stock( productDeprecated.stockQuantity() );
        infoDeprecated.originalPrice( moneyMapper.toMoney( productDeprecatedUnitPriceOriginalPrice( productDeprecated ) ) );
        infoDeprecated.discountRate( productDeprecatedUnitPriceDiscountRate( productDeprecated ) );
        infoDeprecated.discountAmount( moneyMapper.toMoney( productDeprecatedUnitPriceDiscountAmount( productDeprecated ) ) );
        infoDeprecated.discountedPrice( moneyMapper.toMoney( productDeprecatedUnitPriceDiscountedPrice( productDeprecated ) ) );
        infoDeprecated.options( productOptionListToOptionList( productDeprecated.itemOptions() ) );
        infoDeprecated.productId( productDeprecated.productId() );
        infoDeprecated.productVariantId( productDeprecated.productVariantId() );
        infoDeprecated.status( translateStatus( productDeprecated.status() ) );
        infoDeprecated.sku( productDeprecated.sku() );
        infoDeprecated.productName( productDeprecated.productName() );
        infoDeprecated.thumbnail( productDeprecated.thumbnail() );

        return infoDeprecated.build();
    }

    @Override
    public OrderSheetProductResult.Info toResult(ProductClientResponse.Product product) {
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

    private Long productDeprecatedUnitPriceOriginalPrice(ProductClientResponse.ProductDeprecated productDeprecated) {
        ProductClientResponse.UnitPrice unitPrice = productDeprecated.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.originalPrice();
    }

    private Integer productDeprecatedUnitPriceDiscountRate(ProductClientResponse.ProductDeprecated productDeprecated) {
        ProductClientResponse.UnitPrice unitPrice = productDeprecated.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountRate();
    }

    private Long productDeprecatedUnitPriceDiscountAmount(ProductClientResponse.ProductDeprecated productDeprecated) {
        ProductClientResponse.UnitPrice unitPrice = productDeprecated.unitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        return unitPrice.discountAmount();
    }

    private Long productDeprecatedUnitPriceDiscountedPrice(ProductClientResponse.ProductDeprecated productDeprecated) {
        ProductClientResponse.UnitPrice unitPrice = productDeprecated.unitPrice();
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
}
