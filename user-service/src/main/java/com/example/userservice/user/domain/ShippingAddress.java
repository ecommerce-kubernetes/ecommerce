package com.example.userservice.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String receiverName;

    private String receiverPhone;

    private String zipCode;

    private String address;

    private String addressDetail;

    @Builder(access = AccessLevel.PRIVATE)
    private ShippingAddress(Long id, User user, String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        this.id = id;
        this.user = user;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    static ShippingAddress create(Long id, User user, String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
        Assert.notNull(id, "배송지를 생성할때 아이디는 필수이다.");
        Assert.notNull(user, "배송지를 생성할때 유저는 필수이다.");
        Assert.hasText(receiverName, "배송지를 생성할때 수령인 이름은 필수이다.");
        Assert.hasText(receiverPhone, "배송지를 생성할때 수령인 전화번호는 필수이다.");
        Assert.hasText(zipCode, "배송지를 생성할때 우편번호는 필수이다.");
        Assert.hasText(address, "배송지를 생성할때 주소는 필수이다.");
        Assert.hasText(addressDetail, "배송지를 생성할때 상세주소는 필수이다.");

        return ShippingAddress.builder()
                .id(id)
                .user(user)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .build();
    }
}
