package com.example.order_service.payment.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드"),
    EASY_PAYMENT("간편 결제"),
    PHONE("휴대폰"),
    ACCOUNT_TRANSFER("계좌 이체"),
    CULTURE_GIFT_CERTIFICATE("문화 상품권"),
    BOOK_CULTURE_GIFT_CERTIFICATE("도서 문화 상품권"),
    GAME_CULTURE_GIFT_CERTIFICATE("게임 문화 상품권"),
    VIRTUAL_ACCOUNT("가상 계좌");
    private final String description;
}
