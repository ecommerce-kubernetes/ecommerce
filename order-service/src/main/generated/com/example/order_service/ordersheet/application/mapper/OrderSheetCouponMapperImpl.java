package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-14T02:26:08+0900",
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
    public OrderSheetCouponResult.Info toResult(CouponClientResponse.CouponInfo coupon) {
        if ( coupon == null ) {
            return null;
        }

        OrderSheetCouponResult.Info.InfoBuilder info = OrderSheetCouponResult.Info.builder();

        info.couponId( coupon.couponId() );
        info.couponName( coupon.couponName() );
        info.scope( coupon.scope() );
        info.available( coupon.available() );
        info.discountAmount( moneyMapper.toMoney( coupon.discountAmount() ) );

        return info.build();
    }
}
