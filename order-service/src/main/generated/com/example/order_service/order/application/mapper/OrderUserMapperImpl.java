package com.example.order_service.order.application.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-25T20:24:38+0900",
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
    public OrderUserResult.OrdererInfo toResult(UserClientResponse.UserInfo user) {
        if ( user == null ) {
            return null;
        }

        OrderUserResult.OrdererInfo.OrdererInfoBuilder ordererInfo = OrderUserResult.OrdererInfo.builder();

        ordererInfo.availablePoints( user.pointBalance() );
        ordererInfo.ordererName( user.userName() );
        ordererInfo.ordererPhone( user.phoneNumber() );
        ordererInfo.userId( user.userId() );

        return ordererInfo.build();
    }

    @Override
    public OrderUserResult.UserPoint toResult(UserClientResponse.UserPoints points) {
        if ( points == null ) {
            return null;
        }

        OrderUserResult.UserPoint.UserPointBuilder userPoint = OrderUserResult.UserPoint.builder();

        userPoint.userId( points.userId() );
        userPoint.ownedPoints( moneyMapper.toMoney( points.ownedPoints() ) );
        userPoint.availablePoints( moneyMapper.toMoney( points.availablePoints() ) );

        return userPoint.build();
    }
}
