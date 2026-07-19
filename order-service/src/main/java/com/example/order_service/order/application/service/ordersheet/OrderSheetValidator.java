package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.exception.OrderErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Deprecated
@Component
public class OrderSheetValidator {
    public void validate(OrderProductResult productList, OrderSheetCommand.Create command) {
        Map<Long, Integer> reqItemMap = command.toQuantityMap();
        Map<Long, OrderProductResult.OrderProductDetail> productsMap = productList.getProductsMap();
        reqItemMap.forEach((reqId, reqQuantity) -> {
            OrderProductResult.OrderProductDetail product = Optional.ofNullable(productsMap.get(reqId))
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND));
            if (product.status() != OrderProductStatus.ON_SALE) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
            }
            if (reqQuantity > product.stock()) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
            }
        });
    }
}
