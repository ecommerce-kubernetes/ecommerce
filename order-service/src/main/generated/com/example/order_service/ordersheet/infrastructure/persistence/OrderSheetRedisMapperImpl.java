package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemProductSnapshot;
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
class OrderSheetRedisMapperImpl implements OrderSheetRedisMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public OrderSheetRedisMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public OrderSheetRedisEntity toEntity(OrderSheet domain) {
        if ( domain == null ) {
            return null;
        }

        OrderSheetRedisEntity.OrderSheetRedisEntityBuilder orderSheetRedisEntity = OrderSheetRedisEntity.builder();

        orderSheetRedisEntity.sheetId( domain.getSheetId() );
        orderSheetRedisEntity.items( orderSheetItemListToOrderSheetItemRedisEntityList( domain.getItems() ) );
        orderSheetRedisEntity.totalOriginalPrice( moneyMapper.toLong( domain.getTotalOriginalPrice() ) );
        orderSheetRedisEntity.totalProductDiscountAmount( moneyMapper.toLong( domain.getTotalProductDiscountAmount() ) );
        orderSheetRedisEntity.totalPaymentAmount( moneyMapper.toLong( domain.getTotalPaymentAmount() ) );
        orderSheetRedisEntity.expiresAt( domain.getExpiresAt() );

        return orderSheetRedisEntity.build();
    }

    @Override
    public OrderSheet toDomain(OrderSheetRedisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        OrderSheet.OrderSheetBuilder orderSheet = OrderSheet.reconstitute();

        orderSheet.sheetId( entity.getSheetId() );
        orderSheet.items( orderSheetItemRedisEntityListToOrderSheetItemList( entity.getItems() ) );
        orderSheet.totalOriginalPrice( moneyMapper.toMoney( entity.getTotalOriginalPrice() ) );
        orderSheet.totalProductDiscountAmount( moneyMapper.toMoney( entity.getTotalProductDiscountAmount() ) );
        orderSheet.totalPaymentAmount( moneyMapper.toMoney( entity.getTotalPaymentAmount() ) );
        orderSheet.expiresAt( entity.getExpiresAt() );

        return orderSheet.build();
    }

    @Override
    public OrderSheetRedisEntity.OrderSheetItemRedisEntity toItemEntity(OrderSheetItem domain) {
        if ( domain == null ) {
            return null;
        }

        OrderSheetRedisEntity.OrderSheetItemRedisEntity.OrderSheetItemRedisEntityBuilder orderSheetItemRedisEntity = OrderSheetRedisEntity.OrderSheetItemRedisEntity.builder();

        orderSheetItemRedisEntity.priceSnapshot( orderSheetItemPriceSnapshotToPriceSnapshotRedisEntity( domain.getItemPrice() ) );
        orderSheetItemRedisEntity.productSnapshot( orderSheetItemProductSnapshotToProductSnapshotRedisEntity( domain.getProductSnapshot() ) );
        if ( domain.getQuantity() != null ) {
            orderSheetItemRedisEntity.quantity( domain.getQuantity() );
        }
        orderSheetItemRedisEntity.options( orderSheetItemOptionSnapshotListToOptionSnapshotList( domain.getOptions() ) );

        return orderSheetItemRedisEntity.build();
    }

    @Override
    public OrderSheetItem toItemDomain(OrderSheetRedisEntity.OrderSheetItemRedisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        OrderSheetItem.OrderSheetItemBuilder orderSheetItem = OrderSheetItem.reconstitute();

        orderSheetItem.itemPrice( priceSnapshotRedisEntityToOrderSheetItemPriceSnapshot( entity.getPriceSnapshot() ) );
        orderSheetItem.productSnapshot( productSnapshotRedisEntityToOrderSheetItemProductSnapshot( entity.getProductSnapshot() ) );
        orderSheetItem.quantity( entity.getQuantity() );
        orderSheetItem.options( optionSnapshotListToOrderSheetItemOptionSnapshotList( entity.getOptions() ) );

        return orderSheetItem.build();
    }

    protected List<OrderSheetRedisEntity.OrderSheetItemRedisEntity> orderSheetItemListToOrderSheetItemRedisEntityList(List<OrderSheetItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetRedisEntity.OrderSheetItemRedisEntity> list1 = new ArrayList<OrderSheetRedisEntity.OrderSheetItemRedisEntity>( list.size() );
        for ( OrderSheetItem orderSheetItem : list ) {
            list1.add( toItemEntity( orderSheetItem ) );
        }

        return list1;
    }

    protected List<OrderSheetItem> orderSheetItemRedisEntityListToOrderSheetItemList(List<OrderSheetRedisEntity.OrderSheetItemRedisEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetItem> list1 = new ArrayList<OrderSheetItem>( list.size() );
        for ( OrderSheetRedisEntity.OrderSheetItemRedisEntity orderSheetItemRedisEntity : list ) {
            list1.add( toItemDomain( orderSheetItemRedisEntity ) );
        }

        return list1;
    }

    protected OrderSheetRedisEntity.PriceSnapshotRedisEntity orderSheetItemPriceSnapshotToPriceSnapshotRedisEntity(OrderSheetItemPriceSnapshot orderSheetItemPriceSnapshot) {
        if ( orderSheetItemPriceSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.PriceSnapshotRedisEntity.PriceSnapshotRedisEntityBuilder priceSnapshotRedisEntity = OrderSheetRedisEntity.PriceSnapshotRedisEntity.builder();

        priceSnapshotRedisEntity.originalPrice( moneyMapper.toLong( orderSheetItemPriceSnapshot.getOriginalPrice() ) );
        if ( orderSheetItemPriceSnapshot.getDiscountRate() != null ) {
            priceSnapshotRedisEntity.discountRate( orderSheetItemPriceSnapshot.getDiscountRate() );
        }
        priceSnapshotRedisEntity.discountAmount( moneyMapper.toLong( orderSheetItemPriceSnapshot.getDiscountAmount() ) );
        priceSnapshotRedisEntity.discountedPrice( moneyMapper.toLong( orderSheetItemPriceSnapshot.getDiscountedPrice() ) );

        return priceSnapshotRedisEntity.build();
    }

    protected OrderSheetRedisEntity.ProductSnapshotRedisEntity orderSheetItemProductSnapshotToProductSnapshotRedisEntity(OrderSheetItemProductSnapshot orderSheetItemProductSnapshot) {
        if ( orderSheetItemProductSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.ProductSnapshotRedisEntity.ProductSnapshotRedisEntityBuilder productSnapshotRedisEntity = OrderSheetRedisEntity.ProductSnapshotRedisEntity.builder();

        productSnapshotRedisEntity.productId( orderSheetItemProductSnapshot.getProductId() );
        productSnapshotRedisEntity.productVariantId( orderSheetItemProductSnapshot.getProductVariantId() );
        productSnapshotRedisEntity.sku( orderSheetItemProductSnapshot.getSku() );
        productSnapshotRedisEntity.productName( orderSheetItemProductSnapshot.getProductName() );
        productSnapshotRedisEntity.thumbnail( orderSheetItemProductSnapshot.getThumbnail() );

        return productSnapshotRedisEntity.build();
    }

    protected OrderSheetRedisEntity.OptionSnapshot orderSheetItemOptionSnapshotToOptionSnapshot(OrderSheetItemOptionSnapshot orderSheetItemOptionSnapshot) {
        if ( orderSheetItemOptionSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.OptionSnapshot.OptionSnapshotBuilder optionSnapshot = OrderSheetRedisEntity.OptionSnapshot.builder();

        optionSnapshot.optionTypeName( orderSheetItemOptionSnapshot.getOptionTypeName() );
        optionSnapshot.optionValueName( orderSheetItemOptionSnapshot.getOptionValueName() );

        return optionSnapshot.build();
    }

    protected List<OrderSheetRedisEntity.OptionSnapshot> orderSheetItemOptionSnapshotListToOptionSnapshotList(List<OrderSheetItemOptionSnapshot> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetRedisEntity.OptionSnapshot> list1 = new ArrayList<OrderSheetRedisEntity.OptionSnapshot>( list.size() );
        for ( OrderSheetItemOptionSnapshot orderSheetItemOptionSnapshot : list ) {
            list1.add( orderSheetItemOptionSnapshotToOptionSnapshot( orderSheetItemOptionSnapshot ) );
        }

        return list1;
    }

    protected OrderSheetItemPriceSnapshot priceSnapshotRedisEntityToOrderSheetItemPriceSnapshot(OrderSheetRedisEntity.PriceSnapshotRedisEntity priceSnapshotRedisEntity) {
        if ( priceSnapshotRedisEntity == null ) {
            return null;
        }

        OrderSheetItemPriceSnapshot.OrderSheetItemPriceSnapshotBuilder orderSheetItemPriceSnapshot = OrderSheetItemPriceSnapshot.reconstitute();

        orderSheetItemPriceSnapshot.originalPrice( moneyMapper.toMoney( priceSnapshotRedisEntity.getOriginalPrice() ) );
        orderSheetItemPriceSnapshot.discountRate( priceSnapshotRedisEntity.getDiscountRate() );
        orderSheetItemPriceSnapshot.discountAmount( moneyMapper.toMoney( priceSnapshotRedisEntity.getDiscountAmount() ) );
        orderSheetItemPriceSnapshot.discountedPrice( moneyMapper.toMoney( priceSnapshotRedisEntity.getDiscountedPrice() ) );

        return orderSheetItemPriceSnapshot.build();
    }

    protected OrderSheetItemProductSnapshot productSnapshotRedisEntityToOrderSheetItemProductSnapshot(OrderSheetRedisEntity.ProductSnapshotRedisEntity productSnapshotRedisEntity) {
        if ( productSnapshotRedisEntity == null ) {
            return null;
        }

        OrderSheetItemProductSnapshot.OrderSheetItemProductSnapshotBuilder orderSheetItemProductSnapshot = OrderSheetItemProductSnapshot.reconstitute();

        orderSheetItemProductSnapshot.productId( productSnapshotRedisEntity.getProductId() );
        orderSheetItemProductSnapshot.productVariantId( productSnapshotRedisEntity.getProductVariantId() );
        orderSheetItemProductSnapshot.sku( productSnapshotRedisEntity.getSku() );
        orderSheetItemProductSnapshot.productName( productSnapshotRedisEntity.getProductName() );
        orderSheetItemProductSnapshot.thumbnail( productSnapshotRedisEntity.getThumbnail() );

        return orderSheetItemProductSnapshot.build();
    }

    protected OrderSheetItemOptionSnapshot optionSnapshotToOrderSheetItemOptionSnapshot(OrderSheetRedisEntity.OptionSnapshot optionSnapshot) {
        if ( optionSnapshot == null ) {
            return null;
        }

        OrderSheetItemOptionSnapshot.OrderSheetItemOptionSnapshotBuilder orderSheetItemOptionSnapshot = OrderSheetItemOptionSnapshot.reconstitute();

        orderSheetItemOptionSnapshot.optionTypeName( optionSnapshot.getOptionTypeName() );
        orderSheetItemOptionSnapshot.optionValueName( optionSnapshot.getOptionValueName() );

        return orderSheetItemOptionSnapshot.build();
    }

    protected List<OrderSheetItemOptionSnapshot> optionSnapshotListToOrderSheetItemOptionSnapshotList(List<OrderSheetRedisEntity.OptionSnapshot> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetItemOptionSnapshot> list1 = new ArrayList<OrderSheetItemOptionSnapshot>( list.size() );
        for ( OrderSheetRedisEntity.OptionSnapshot optionSnapshot : list ) {
            list1.add( optionSnapshotToOrderSheetItemOptionSnapshot( optionSnapshot ) );
        }

        return list1;
    }
}
