package com.example.order_service.ordersheet.application;

import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.infrastructure.client.OrderSheetAdaptor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OrderSheetProductService {
    private final OrderSheetAdaptor adaptor;

    public List<OrderSheetProductResult.Info> getProducts(List<Long> productVariantIds) {
        return null;
    }
}
