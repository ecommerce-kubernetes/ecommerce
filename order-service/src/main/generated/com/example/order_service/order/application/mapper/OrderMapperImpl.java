package com.example.order_service.order.application.mapper;

import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-25T02:06:40+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderContext.CreateOrderContext toContext(OrderSheet orderSheet) {
        if ( orderSheet == null ) {
            return null;
        }

        OrderContext.CreateOrderContext.CreateOrderContextBuilder createOrderContext = OrderContext.CreateOrderContext.builder();

        createOrderContext.orderItems( orderSheetItemListToItemContextList( orderSheet.getItems() ) );
        createOrderContext.orderer( orderSheet.getOrderer() );
        createOrderContext.shippingAddress( orderSheet.getShippingAddress() );
        if ( orderSheet.hasCartCoupon() ) {
            createOrderContext.cartCoupon( orderSheet.getCartCoupon() );
        }
        createOrderContext.totalOriginalPrice( orderSheet.getTotalOriginalPrice() );
        createOrderContext.totalProductDiscountAmount( orderSheet.getTotalProductDiscountAmount() );
        createOrderContext.totalCouponDiscountAmount( orderSheet.getTotalCouponDiscountAmount() );
        createOrderContext.usedPoints( orderSheet.getUsedPoints() );
        createOrderContext.totalPaymentAmount( orderSheet.getTotalPaymentAmount() );

        return createOrderContext.build();
    }

    @Override
    public OrderContext.ItemContext toItemContext(OrderSheetItem orderSheetItem) {
        if ( orderSheetItem == null ) {
            return null;
        }

        OrderContext.ItemContext.ItemContextBuilder itemContext = OrderContext.ItemContext.builder();

        itemContext.productSnapshot( orderSheetItem.getProductSnapshot() );
        itemContext.itemPrice( orderSheetItem.getItemPrice() );
        itemContext.itemCoupon( orderSheetItem.getItemCoupon() );
        itemContext.quantity( orderSheetItem.getQuantity() );
        List<ProductOptionSnapshot> list = orderSheetItem.getOptions();
        if ( list != null ) {
            itemContext.options( new ArrayList<ProductOptionSnapshot>( list ) );
        }

        return itemContext.build();
    }

    protected List<OrderContext.ItemContext> orderSheetItemListToItemContextList(List<OrderSheetItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderContext.ItemContext> list1 = new ArrayList<OrderContext.ItemContext>( list.size() );
        for ( OrderSheetItem orderSheetItem : list ) {
            list1.add( toItemContext( orderSheetItem ) );
        }

        return list1;
    }
}
