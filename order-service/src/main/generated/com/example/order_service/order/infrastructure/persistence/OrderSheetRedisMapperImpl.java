package com.example.order_service.order.infrastructure.persistence;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-01T00:06:02+0900",
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
        orderSheetRedisEntity.orderer( toOrdererEntity( domain.getOrderer() ) );
        orderSheetRedisEntity.shippingAddress( toShippingAddressEntity( domain.getShippingAddress() ) );
        orderSheetRedisEntity.items( orderSheetItemListToOrderSheetItemRedisEntityList( domain.getItems() ) );
        if ( domain.hasCartCoupon() ) {
            orderSheetRedisEntity.cartCoupon( orderCouponSnapshotToCouponSnapshotRedisEntity( domain.getCartCoupon() ) );
        }
        orderSheetRedisEntity.totalOriginalPrice( moneyMapper.toLong( domain.getTotalOriginalPrice() ) );
        orderSheetRedisEntity.totalProductDiscountAmount( moneyMapper.toLong( domain.getTotalProductDiscountAmount() ) );
        orderSheetRedisEntity.totalCouponDiscountAmount( moneyMapper.toLong( domain.getTotalCouponDiscountAmount() ) );
        orderSheetRedisEntity.usedPoints( moneyMapper.toLong( domain.getUsedPoints() ) );
        orderSheetRedisEntity.totalPaymentAmount( moneyMapper.toLong( domain.getTotalPaymentAmount() ) );
        orderSheetRedisEntity.expiresAt( domain.getExpiresAt() );

        return orderSheetRedisEntity.build();
    }

    @Override
    public OrderSheet toDomain(OrderSheetRedisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        OrderSheet.OrderSheetBuilder orderSheet = createOrderSheetBuilder();

        orderSheet.sheetId( entity.getSheetId() );
        orderSheet.orderer( toOrdererDomain( entity.getOrderer() ) );
        orderSheet.shippingAddress( toShippingAddressDomain( entity.getShippingAddress() ) );
        orderSheet.items( orderSheetItemRedisEntityListToOrderSheetItemList( entity.getItems() ) );
        orderSheet.cartCoupon( couponSnapshotRedisEntityToOrderCouponSnapshot( entity.getCartCoupon() ) );
        orderSheet.totalOriginalPrice( moneyMapper.toMoney( entity.getTotalOriginalPrice() ) );
        orderSheet.totalProductDiscountAmount( moneyMapper.toMoney( entity.getTotalProductDiscountAmount() ) );
        orderSheet.totalCouponDiscountAmount( moneyMapper.toMoney( entity.getTotalCouponDiscountAmount() ) );
        orderSheet.usedPoints( moneyMapper.toMoney( entity.getUsedPoints() ) );
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

        orderSheetItemRedisEntity.priceSnapshot( productPriceSnapshotToPriceSnapshotRedisEntity( domain.getItemPrice() ) );
        orderSheetItemRedisEntity.sheetItemId( domain.getSheetItemId() );
        orderSheetItemRedisEntity.productSnapshot( productSnapshotToProductSnapshotRedisEntity( domain.getProductSnapshot() ) );
        orderSheetItemRedisEntity.itemCoupon( orderCouponSnapshotToCouponSnapshotRedisEntity( domain.getItemCoupon() ) );
        if ( domain.getQuantity() != null ) {
            orderSheetItemRedisEntity.quantity( domain.getQuantity() );
        }
        orderSheetItemRedisEntity.options( productOptionSnapshotListToOptionSnapshotList( domain.getOptions() ) );

        return orderSheetItemRedisEntity.build();
    }

    @Override
    public OrderSheetItem toItemDomain(OrderSheetRedisEntity.OrderSheetItemRedisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        OrderSheetItem.OrderSheetItemBuilder orderSheetItem = createOrderSheetItemBuilder();

        orderSheetItem.itemPrice( priceSnapshotRedisEntityToProductPriceSnapshot( entity.getPriceSnapshot() ) );
        orderSheetItem.sheetItemId( entity.getSheetItemId() );
        orderSheetItem.productSnapshot( productSnapshotRedisEntityToProductSnapshot( entity.getProductSnapshot() ) );
        orderSheetItem.itemCoupon( couponSnapshotRedisEntityToOrderCouponSnapshot( entity.getItemCoupon() ) );
        orderSheetItem.quantity( entity.getQuantity() );
        orderSheetItem.options( optionSnapshotListToProductOptionSnapshotList( entity.getOptions() ) );

        return orderSheetItem.build();
    }

    @Override
    public OrderSheetRedisEntity.OrderSheetOrdererRedisEntity toOrdererEntity(Orderer domain) {
        if ( domain == null ) {
            return null;
        }

        OrderSheetRedisEntity.OrderSheetOrdererRedisEntity.OrderSheetOrdererRedisEntityBuilder orderSheetOrdererRedisEntity = OrderSheetRedisEntity.OrderSheetOrdererRedisEntity.builder();

        orderSheetOrdererRedisEntity.userId( domain.getUserId() );
        orderSheetOrdererRedisEntity.userName( domain.getUserName() );
        orderSheetOrdererRedisEntity.phoneNumber( domain.getPhoneNumber() );

        return orderSheetOrdererRedisEntity.build();
    }

    @Override
    public Orderer toOrdererDomain(OrderSheetRedisEntity.OrderSheetOrdererRedisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Orderer.OrdererBuilder orderer = Orderer.reconstitute();

        orderer.userId( entity.getUserId() );
        orderer.userName( entity.getUserName() );
        orderer.phoneNumber( entity.getPhoneNumber() );

        return orderer.build();
    }

    @Override
    public OrderSheetRedisEntity.OrderSheetShippingAddressRedisEntity toShippingAddressEntity(ShippingAddress domain) {
        if ( domain == null ) {
            return null;
        }

        OrderSheetRedisEntity.OrderSheetShippingAddressRedisEntity.OrderSheetShippingAddressRedisEntityBuilder orderSheetShippingAddressRedisEntity = OrderSheetRedisEntity.OrderSheetShippingAddressRedisEntity.builder();

        orderSheetShippingAddressRedisEntity.receiverName( domain.getReceiverName() );
        orderSheetShippingAddressRedisEntity.receiverPhone( domain.getReceiverPhone() );
        orderSheetShippingAddressRedisEntity.zipCode( domain.getZipCode() );
        orderSheetShippingAddressRedisEntity.address( domain.getAddress() );
        orderSheetShippingAddressRedisEntity.addressDetail( domain.getAddressDetail() );

        return orderSheetShippingAddressRedisEntity.build();
    }

    @Override
    public ShippingAddress toShippingAddressDomain(OrderSheetRedisEntity.OrderSheetShippingAddressRedisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ShippingAddress.ShippingAddressBuilder shippingAddress = ShippingAddress.reconstitute();

        shippingAddress.receiverName( entity.getReceiverName() );
        shippingAddress.receiverPhone( entity.getReceiverPhone() );
        shippingAddress.zipCode( entity.getZipCode() );
        shippingAddress.address( entity.getAddress() );
        shippingAddress.addressDetail( entity.getAddressDetail() );

        return shippingAddress.build();
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

    protected OrderSheetRedisEntity.CouponSnapshotRedisEntity orderCouponSnapshotToCouponSnapshotRedisEntity(OrderCouponSnapshot orderCouponSnapshot) {
        if ( orderCouponSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.CouponSnapshotRedisEntity.CouponSnapshotRedisEntityBuilder couponSnapshotRedisEntity = OrderSheetRedisEntity.CouponSnapshotRedisEntity.builder();

        couponSnapshotRedisEntity.couponId( orderCouponSnapshot.getCouponId() );
        couponSnapshotRedisEntity.couponName( orderCouponSnapshot.getCouponName() );
        couponSnapshotRedisEntity.discountAmount( moneyMapper.toLong( orderCouponSnapshot.getDiscountAmount() ) );

        return couponSnapshotRedisEntity.build();
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

    protected OrderCouponSnapshot couponSnapshotRedisEntityToOrderCouponSnapshot(OrderSheetRedisEntity.CouponSnapshotRedisEntity couponSnapshotRedisEntity) {
        if ( couponSnapshotRedisEntity == null ) {
            return null;
        }

        OrderCouponSnapshot.OrderCouponSnapshotBuilder orderCouponSnapshot = OrderCouponSnapshot.reconstitute();

        orderCouponSnapshot.couponId( couponSnapshotRedisEntity.getCouponId() );
        orderCouponSnapshot.couponName( couponSnapshotRedisEntity.getCouponName() );
        orderCouponSnapshot.discountAmount( moneyMapper.toMoney( couponSnapshotRedisEntity.getDiscountAmount() ) );

        return orderCouponSnapshot.build();
    }

    protected OrderSheetRedisEntity.PriceSnapshotRedisEntity productPriceSnapshotToPriceSnapshotRedisEntity(ProductPriceSnapshot productPriceSnapshot) {
        if ( productPriceSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.PriceSnapshotRedisEntity.PriceSnapshotRedisEntityBuilder priceSnapshotRedisEntity = OrderSheetRedisEntity.PriceSnapshotRedisEntity.builder();

        priceSnapshotRedisEntity.originalPrice( moneyMapper.toLong( productPriceSnapshot.getOriginalPrice() ) );
        if ( productPriceSnapshot.getDiscountRate() != null ) {
            priceSnapshotRedisEntity.discountRate( productPriceSnapshot.getDiscountRate() );
        }
        priceSnapshotRedisEntity.discountAmount( moneyMapper.toLong( productPriceSnapshot.getDiscountAmount() ) );
        priceSnapshotRedisEntity.discountedPrice( moneyMapper.toLong( productPriceSnapshot.getDiscountedPrice() ) );

        return priceSnapshotRedisEntity.build();
    }

    protected OrderSheetRedisEntity.ProductSnapshotRedisEntity productSnapshotToProductSnapshotRedisEntity(ProductSnapshot productSnapshot) {
        if ( productSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.ProductSnapshotRedisEntity.ProductSnapshotRedisEntityBuilder productSnapshotRedisEntity = OrderSheetRedisEntity.ProductSnapshotRedisEntity.builder();

        productSnapshotRedisEntity.productId( productSnapshot.getProductId() );
        productSnapshotRedisEntity.productVariantId( productSnapshot.getProductVariantId() );
        productSnapshotRedisEntity.sku( productSnapshot.getSku() );
        productSnapshotRedisEntity.productName( productSnapshot.getProductName() );
        productSnapshotRedisEntity.thumbnail( productSnapshot.getThumbnail() );

        return productSnapshotRedisEntity.build();
    }

    protected OrderSheetRedisEntity.OptionSnapshot productOptionSnapshotToOptionSnapshot(ProductOptionSnapshot productOptionSnapshot) {
        if ( productOptionSnapshot == null ) {
            return null;
        }

        OrderSheetRedisEntity.OptionSnapshot.OptionSnapshotBuilder optionSnapshot = OrderSheetRedisEntity.OptionSnapshot.builder();

        optionSnapshot.optionTypeName( productOptionSnapshot.getOptionTypeName() );
        optionSnapshot.optionValueName( productOptionSnapshot.getOptionValueName() );

        return optionSnapshot.build();
    }

    protected List<OrderSheetRedisEntity.OptionSnapshot> productOptionSnapshotListToOptionSnapshotList(List<ProductOptionSnapshot> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetRedisEntity.OptionSnapshot> list1 = new ArrayList<OrderSheetRedisEntity.OptionSnapshot>( list.size() );
        for ( ProductOptionSnapshot productOptionSnapshot : list ) {
            list1.add( productOptionSnapshotToOptionSnapshot( productOptionSnapshot ) );
        }

        return list1;
    }

    protected ProductPriceSnapshot priceSnapshotRedisEntityToProductPriceSnapshot(OrderSheetRedisEntity.PriceSnapshotRedisEntity priceSnapshotRedisEntity) {
        if ( priceSnapshotRedisEntity == null ) {
            return null;
        }

        ProductPriceSnapshot.ProductPriceSnapshotBuilder productPriceSnapshot = ProductPriceSnapshot.reconstitute();

        productPriceSnapshot.originalPrice( moneyMapper.toMoney( priceSnapshotRedisEntity.getOriginalPrice() ) );
        productPriceSnapshot.discountRate( priceSnapshotRedisEntity.getDiscountRate() );
        productPriceSnapshot.discountAmount( moneyMapper.toMoney( priceSnapshotRedisEntity.getDiscountAmount() ) );
        productPriceSnapshot.discountedPrice( moneyMapper.toMoney( priceSnapshotRedisEntity.getDiscountedPrice() ) );

        return productPriceSnapshot.build();
    }

    protected ProductSnapshot productSnapshotRedisEntityToProductSnapshot(OrderSheetRedisEntity.ProductSnapshotRedisEntity productSnapshotRedisEntity) {
        if ( productSnapshotRedisEntity == null ) {
            return null;
        }

        ProductSnapshot.ProductSnapshotBuilder productSnapshot = ProductSnapshot.reconstitute();

        productSnapshot.productId( productSnapshotRedisEntity.getProductId() );
        productSnapshot.productVariantId( productSnapshotRedisEntity.getProductVariantId() );
        productSnapshot.sku( productSnapshotRedisEntity.getSku() );
        productSnapshot.productName( productSnapshotRedisEntity.getProductName() );
        productSnapshot.thumbnail( productSnapshotRedisEntity.getThumbnail() );

        return productSnapshot.build();
    }

    protected ProductOptionSnapshot optionSnapshotToProductOptionSnapshot(OrderSheetRedisEntity.OptionSnapshot optionSnapshot) {
        if ( optionSnapshot == null ) {
            return null;
        }

        ProductOptionSnapshot.ProductOptionSnapshotBuilder productOptionSnapshot = ProductOptionSnapshot.reconstitute();

        productOptionSnapshot.optionTypeName( optionSnapshot.getOptionTypeName() );
        productOptionSnapshot.optionValueName( optionSnapshot.getOptionValueName() );

        return productOptionSnapshot.build();
    }

    protected List<ProductOptionSnapshot> optionSnapshotListToProductOptionSnapshotList(List<OrderSheetRedisEntity.OptionSnapshot> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductOptionSnapshot> list1 = new ArrayList<ProductOptionSnapshot>( list.size() );
        for ( OrderSheetRedisEntity.OptionSnapshot optionSnapshot : list ) {
            list1.add( optionSnapshotToProductOptionSnapshot( optionSnapshot ) );
        }

        return list1;
    }
}
