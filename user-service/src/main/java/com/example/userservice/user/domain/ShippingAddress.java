package com.example.userservice.user.domain;

import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import jakarta.persistence.*;
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

    private boolean isDefault;

    @Builder(access = AccessLevel.PRIVATE)
    private ShippingAddress(Long id, String receiverName, String receiverPhone, String zipCode, String address, String addressDetail, boolean isDefault) {
        Assert.notNull(id, "배송지를 생성할때 아이디는 필수이다.");
        Assert.hasText(receiverName, "배송지를 생성할때 수령인 이름은 필수이다.");
        Assert.hasText(receiverPhone, "배송지를 생성할때 수령인 전화번호는 필수이다.");
        Assert.hasText(zipCode, "배송지를 생성할때 우편번호는 필수이다.");
        Assert.hasText(address, "배송지를 생성할때 주소는 필수이다.");
        Assert.hasText(addressDetail, "배송지를 생성할때 상세주소는 필수이다.");
        Assert.notNull(isDefault, "배송지를 생성할때 대표 배송지 여부는 필수이다.");

        this.id = id;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

    static ShippingAddress create(CreateShippingAddressContext context, boolean isDefault) {
        return ShippingAddress.builder()
                .id(context.id())
                .receiverName(context.receiverName())
                .receiverPhone(context.receiverPhone())
                .zipCode(context.zipCode())
                .address(context.address())
                .addressDetail(context.addressDetail())
                .isDefault(isDefault)
                .build();
    }

    void promoteToDefault() {
        this.isDefault = true;
    }

    void demoteFromDefault() {
        this.isDefault = false;
    }

    void setUser(User user) {
        this.user = user;
    }
}
