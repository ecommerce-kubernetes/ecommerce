package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentDetails {
    private int usedCash;
    private int usedReserve;
}
