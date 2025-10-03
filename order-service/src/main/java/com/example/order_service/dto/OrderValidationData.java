package com.example.order_service.dto;

import com.example.order_service.service.client.dto.CouponResponse;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public class OrderValidationData {
    List<ProductResponse> products;
    UserBalanceResponse userBalance;
    CouponResponse coupon;

    public Map<Long, ProductResponse> toProductByVariantId(){
        return products.stream().collect(Collectors.toMap(ProductResponse::getProductVariantId, Function.identity()));
    }
}
