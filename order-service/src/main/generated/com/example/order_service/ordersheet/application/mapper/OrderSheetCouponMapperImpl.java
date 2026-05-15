package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-15T14:01:47+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderSheetCouponMapperImpl implements OrderSheetCouponMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public OrderSheetCouponMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public CouponCommand.Calculate toCommand(OrderSheetCommand.CouponCalculate command) {
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
    public CouponCommand.Item toItemCommand(OrderSheetCommand.AppliedCouponItem command) {
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
    public OrderSheetCouponResult.Calculate toResult(CouponClientResponse.Calculate response) {
        if ( response == null ) {
            return null;
        }

        OrderSheetCouponResult.Calculate.CalculateBuilder calculate = OrderSheetCouponResult.Calculate.builder();

        calculate.cartCoupon( toCartCouponResult( response.cartCoupon() ) );
        calculate.itemCoupons( itemCouponListToItemCouponList( response.itemCoupons() ) );

        return calculate.build();
    }

    @Override
    public OrderSheetCouponResult.CartCoupon toCartCouponResult(CouponClientResponse.CartCoupon response) {
        if ( response == null ) {
            return null;
        }

        OrderSheetCouponResult.CartCoupon.CartCouponBuilder cartCoupon = OrderSheetCouponResult.CartCoupon.builder();

        cartCoupon.couponId( response.couponId() );
        cartCoupon.couponName( response.couponName() );
        cartCoupon.discountAmount( moneyMapper.toMoney( response.discountAmount() ) );

        return cartCoupon.build();
    }

    @Override
    public OrderSheetCouponResult.ItemCoupon toItemCouponResult(CouponClientResponse.ItemCoupon response) {
        if ( response == null ) {
            return null;
        }

        OrderSheetCouponResult.ItemCoupon.ItemCouponBuilder itemCoupon = OrderSheetCouponResult.ItemCoupon.builder();

        itemCoupon.productVariantId( response.productVariantId() );
        itemCoupon.couponId( response.couponId() );
        itemCoupon.couponName( response.couponName() );
        itemCoupon.discountAmount( moneyMapper.toMoney( response.discountAmount() ) );

        return itemCoupon.build();
    }

    protected List<CouponCommand.Item> appliedCouponItemListToItemList(List<OrderSheetCommand.AppliedCouponItem> list) {
        if ( list == null ) {
            return null;
        }

        List<CouponCommand.Item> list1 = new ArrayList<CouponCommand.Item>( list.size() );
        for ( OrderSheetCommand.AppliedCouponItem appliedCouponItem : list ) {
            list1.add( toItemCommand( appliedCouponItem ) );
        }

        return list1;
    }

    protected List<OrderSheetCouponResult.ItemCoupon> itemCouponListToItemCouponList(List<CouponClientResponse.ItemCoupon> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderSheetCouponResult.ItemCoupon> list1 = new ArrayList<OrderSheetCouponResult.ItemCoupon>( list.size() );
        for ( CouponClientResponse.ItemCoupon itemCoupon : list ) {
            list1.add( toItemCouponResult( itemCoupon ) );
        }

        return list1;
    }
}
