package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-17T18:49:09+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderSheetUserMapperImpl implements OrderSheetUserMapper {

    @Override
    public OrderSheetUserResult.Profile toResult(UserClientResponse.Profile profile) {
        if ( profile == null ) {
            return null;
        }

        OrderSheetUserResult.Profile.ProfileBuilder profile1 = OrderSheetUserResult.Profile.builder();

        profile1.shippingAddress( toShippingAddressResult( profile.defaultShippingAddress() ) );
        profile1.userId( profile.userId() );
        profile1.userName( profile.userName() );
        profile1.phoneNumber( profile.phoneNumber() );

        return profile1.build();
    }

    @Override
    public OrderSheetUserResult.ShippingAddress toShippingAddressResult(UserClientResponse.ShippingAddress shippingAddress) {
        if ( shippingAddress == null ) {
            return null;
        }

        OrderSheetUserResult.ShippingAddress.ShippingAddressBuilder shippingAddress1 = OrderSheetUserResult.ShippingAddress.builder();

        shippingAddress1.receiverName( shippingAddress.receiverName() );
        shippingAddress1.receiverPhone( shippingAddress.receiverPhone() );
        shippingAddress1.zipCode( shippingAddress.zipCode() );
        shippingAddress1.address( shippingAddress.address() );
        shippingAddress1.addressDetail( shippingAddress.addressDetail() );

        return shippingAddress1.build();
    }
}
