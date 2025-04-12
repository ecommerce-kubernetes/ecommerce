package com.example.userservice.jpa.AddressEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    @Column(nullable = false, length = 100)
    private String street;  // 도로명 주소 또는 지번 주소

    @Column(nullable = false, length = 50)
    private String city;  // 시/도

    @Column(nullable = false, length = 50)
    private String state; // 구/군

    @Column(nullable = false, length = 10)
    private String zipCode; // 우편번호

    @Column(length = 100)
    private String detail;  // 상세 주소

    public String getAddressAll() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(" ")
                .append(city).append(" ")
                .append(state).append(" ");

        if (detail != null && !detail.isEmpty()) {
            sb.append(detail);
        }

        return sb.toString().trim();
    }
}
