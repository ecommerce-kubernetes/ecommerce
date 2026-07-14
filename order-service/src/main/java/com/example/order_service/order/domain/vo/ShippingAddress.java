package com.example.order_service.order.domain.vo;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import com.mysema.commons.lang.Assert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {
    private String receiverName;
    private String receiverPhone;
    private String zipCode;
    private String address;
    private String addressDetail;

    @Builder(builderMethodName = "reconstitute")
    private ShippingAddress(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        Assert.hasText(receiverName, "배송 정보에 수령인 이름은 필수 입니다.");
        Assert.hasText(receiverPhone, "배송 정보에 수령인 전화번호는 필수 입니다.");
        Assert.hasText(zipCode, "배송 정보에 우편 번호는 필수 입니다.");
        Assert.hasText(address, "배송 정보에 주소는 필수 입니다.");
        Assert.hasText(addressDetail, "배송 정보에 상세 주소는 필수 입니다.");

        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    public static ShippingAddress of(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        if (receiverPhone == null || !receiverPhone.matches("^01[016-9]-\\d{3,4}-\\d{4}$")) {
            throw new BusinessException(OrderErrorCode.INVALID_PHONE_NUMBER);
        }

        if (zipCode == null || !zipCode.matches("^\\d{5}$")) {
            throw new BusinessException(OrderErrorCode.INVALID_ZIPCODE);
        }

        return new ShippingAddress(receiverName, receiverPhone, zipCode, address, addressDetail);
    }
}
