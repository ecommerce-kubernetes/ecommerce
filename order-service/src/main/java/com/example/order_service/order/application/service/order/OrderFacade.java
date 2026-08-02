package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetItem;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderSheetRepository orderSheetRepository;
    private final OrderProductPort orderProductPort;
    private final OrderValidator orderValidator;
    private final Clock clock;

    public OrderCreateResult createOrder(CreateOrderCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        OrderProductsResult products = getProducts(orderSheet);
        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        for (OrderSheetItem item : orderSheet.getItems()) {
            OrderProductsResult.OrderProductDetail product = productsMap.get(item.getProductVariantId());
            orderValidator.validateOrderable(product, item.getQuantity());
            item.validatePriceNotChanged(product.priceSnapshot());
        }
        return null;
    }

    private OrderSheet getValidOrderSheet(Long orderSheetId, Long userId) {
        OrderSheet orderSheet = orderSheetRepository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }

    private OrderProductsResult getProducts(OrderSheet orderSheet) {
        List<Long> productVariantIds = orderSheet.getItems().stream()
                .map(OrderSheetItem::getProductVariantId).toList();
        return orderProductPort.getProducts(productVariantIds);
    }

    private OrderSheet findOrderSheetById(String sheetId) {
        return null;
    }
}
