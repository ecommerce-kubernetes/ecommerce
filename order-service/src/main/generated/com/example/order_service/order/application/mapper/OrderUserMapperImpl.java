package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-03T17:31:14+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderUserMapperImpl implements OrderUserMapper {

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
}
