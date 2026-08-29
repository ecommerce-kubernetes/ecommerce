package com.example.userservice.user.domain;

import jakarta.persistence.Entity;

@Entity
public class ShippingAddress {

    private Long id;

    private User user;

    private String receiverName;

    private String receiverPhone;

    private String zipCode;

    private String address;

    private String addressDetail;
}
