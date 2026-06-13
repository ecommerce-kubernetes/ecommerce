package com.example.order_service.order.domain.vo;

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
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    public static ShippingAddress of(String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        if (receiverName == null || receiverName.isBlank()) {
            throw new IllegalArgumentException("수령인 이름은 필수 입니다");
        }
        if (receiverPhone == null || !receiverPhone.matches("^01[016-9]-\\d{3,4}-\\d{4}$")) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다.");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("우편번호는 필수입니다");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }
        if (addressDetail == null || addressDetail.isBlank()) {
            throw new IllegalArgumentException("상세 주소는 필수입니다");
        }
        return new ShippingAddress(receiverName, receiverPhone, zipCode, address, addressDetail);
    }
}
