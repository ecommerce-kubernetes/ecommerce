package com.example.order_service.ordersheet.application;

import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.Orderer;
import com.example.order_service.ordersheet.domain.model.vo.ShippingAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderSheetFactory {

    public OrderSheet createSheet(OrderSheetCommand.Create command, OrderSheetUserResult.Profile profile,
                             List<OrderSheetProductResult.Info> products, OrderSheetCouponResult.Calculate couponResult, long ttlMinute) {
        String sheetId = generateId();
        Orderer orderer = createOrderer(profile);
        ShippingAddress shippingAddress = createShippingAddress(profile);
        return null;
    }

    private Orderer createOrderer(OrderSheetUserResult.Profile profile) {
        return Orderer.of(profile.userId(), profile.userName(), profile.phoneNumber());
    }

    private ShippingAddress createShippingAddress(OrderSheetUserResult.Profile profile) {
        OrderSheetUserResult.ShippingAddress shippingAddress = profile.shippingAddress();
        return ShippingAddress.of(
                shippingAddress.receiverName(), shippingAddress.receiverPhone(),
                shippingAddress.zipCode(), shippingAddress.address(), shippingAddress.addressDetail()
        );
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
