package com.example.order_service.ordersheet.domain.model.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingAddress {
    private String receiverName;
    private String receiverPhone;
    private String zipCode;
    private String address;
    private String addressDetail;

    @Builder(builderMethodName = "reconstitute")
    private ShippingAddress(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    public static ShippingAddress of(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        return new ShippingAddress(receiverName, receiverPhone, zipCode, address, addressDetail);
    }
}
