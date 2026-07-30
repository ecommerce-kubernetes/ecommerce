package com.example.order_service.order.domain.vo;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {

    private String receiverName;

    private String receiverPhone;

    private String zipCode;

    private String address;

    private String addressDetail;

    private ShippingAddress(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    public static ShippingAddress of(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        Assert.hasText(receiverName, "수령인 이름은 필수 입니다.");
        Assert.hasText(receiverPhone, "수령인 전화번호는 필수 입니다.");
        Assert.hasText(zipCode, "우편 번호는 필수 입니다.");
        Assert.hasText(address, "주소는 필수 입니다.");
        Assert.hasText(addressDetail, "상세 주소는 필수 입니다.");

        if (!receiverPhone.matches("^01[016-9]-\\d{3,4}-\\d{4}$")) {
            throw new BusinessException(OrderErrorCode.INVALID_PHONE_NUMBER);
        }

        if (!zipCode.matches("^\\d{5}$")) {
            throw new BusinessException(OrderErrorCode.INVALID_ZIPCODE);
        }

        return new ShippingAddress(receiverName, receiverPhone, zipCode, address, addressDetail);
    }
}
