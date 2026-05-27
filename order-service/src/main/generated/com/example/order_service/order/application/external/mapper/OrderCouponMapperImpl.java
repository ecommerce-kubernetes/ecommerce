package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T04:04:52+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderCouponMapperImpl implements OrderCouponMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public OrderCouponMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public CouponCommand.Calculate toCommand(OrderCouponCommand.Calculate command) {
        if ( command == null ) {
            return null;
        }

        CouponCommand.Calculate.CalculateBuilder calculate = CouponCommand.Calculate.builder();

        calculate.userId( command.userId() );
        calculate.cartCouponId( command.cartCouponId() );
        calculate.items( appliedCouponItemListToItemList( command.items() ) );

        return calculate.build();
    }

    @Override
    public CouponCommand.Item toItemCommand(OrderCouponCommand.AppliedCouponItem command) {
        if ( command == null ) {
            return null;
        }

        CouponCommand.Item.ItemBuilder item = CouponCommand.Item.builder();

        item.price( moneyMapper.toLong( command.discountedPrice() ) );
        item.productVariantId( command.productVariantId() );
        item.quantity( command.quantity() );
        item.itemCouponId( command.itemCouponId() );

        return item.build();
    }

    @Override
    public OrderCouponResult.Calculate toResult(CouponClientResponse.Calculate response) {
        if ( response == null ) {
            return null;
        }

        OrderCouponResult.Calculate.CalculateBuilder calculate = OrderCouponResult.Calculate.builder();

        calculate.cartCoupon( toCartCouponResult( response.cartCoupon() ) );
        calculate.itemCoupons( itemCouponListToItemCouponList( response.itemCoupons() ) );

        return calculate.build();
    }

    @Override
    public OrderCouponResult.CartCoupon toCartCouponResult(CouponClientResponse.CartCoupon response) {
        if ( response == null ) {
            return null;
        }

        OrderCouponResult.CartCoupon.CartCouponBuilder cartCoupon = OrderCouponResult.CartCoupon.builder();

        cartCoupon.couponId( response.couponId() );
        cartCoupon.couponName( response.couponName() );
        cartCoupon.discountAmount( moneyMapper.toMoney( response.discountAmount() ) );

        return cartCoupon.build();
    }

    @Override
    public OrderCouponResult.ItemCoupon toItemCouponResult(CouponClientResponse.ItemCoupon response) {
        if ( response == null ) {
            return null;
        }

        OrderCouponResult.ItemCoupon.ItemCouponBuilder itemCoupon = OrderCouponResult.ItemCoupon.builder();

        itemCoupon.productVariantId( response.productVariantId() );
        itemCoupon.couponId( response.couponId() );
        itemCoupon.couponName( response.couponName() );
        itemCoupon.discountAmount( moneyMapper.toMoney( response.discountAmount() ) );

        return itemCoupon.build();
    }

    protected List<CouponCommand.Item> appliedCouponItemListToItemList(List<OrderCouponCommand.AppliedCouponItem> list) {
        if ( list == null ) {
            return null;
        }

        List<CouponCommand.Item> list1 = new ArrayList<CouponCommand.Item>( list.size() );
        for ( OrderCouponCommand.AppliedCouponItem appliedCouponItem : list ) {
            list1.add( toItemCommand( appliedCouponItem ) );
        }

        return list1;
    }

    protected List<OrderCouponResult.ItemCoupon> itemCouponListToItemCouponList(List<CouponClientResponse.ItemCoupon> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderCouponResult.ItemCoupon> list1 = new ArrayList<OrderCouponResult.ItemCoupon>( list.size() );
        for ( CouponClientResponse.ItemCoupon itemCoupon : list ) {
            list1.add( toItemCouponResult( itemCoupon ) );
        }

        return list1;
    }
}
