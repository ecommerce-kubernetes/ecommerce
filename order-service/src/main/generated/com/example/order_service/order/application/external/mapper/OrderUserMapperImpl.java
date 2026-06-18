package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T04:49:14+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderUserMapperImpl implements OrderUserMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public OrderUserMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public OrderUserResult.Profile toResult(UserClientResponse.Profile profile) {
        if ( profile == null ) {
            return null;
        }

        OrderUserResult.Profile.ProfileBuilder profile1 = OrderUserResult.Profile.builder();

        profile1.shippingAddress( toShippingAddress( profile.defaultShippingInfo() ) );
        profile1.orderer( toOrderer( profile ) );

        return profile1.build();
    }

    @Override
    public OrderUserResult.UserPoint toResult(UserClientResponse.UserPoints points) {
        if ( points == null ) {
            return null;
        }

        OrderUserResult.UserPoint.UserPointBuilder userPoint = OrderUserResult.UserPoint.builder();

        userPoint.userId( points.userId() );
        userPoint.ownedPoints( moneyMapper.toMoney( points.ownedPoints() ) );

        return userPoint.build();
    }
}
