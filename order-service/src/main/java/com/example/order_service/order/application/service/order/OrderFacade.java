package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.OrderUserPort;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
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
    private final OrderCommandService orderCommandService;
    private final OrderProductPort orderProductPort;
    private final OrderCouponPort orderCouponPort;
    private final OrderUserPort orderUserPort;
    private final PointUsagePolicy pointUsagePolicy;
    private final OrderValidator orderValidator;
    private final OrderContextFactory orderContextFactory;
    private final Clock clock;

    public OrderCreateResult createOrder(CreateOrderCommand command) {
        LocalDateTime currentTime = LocalDateTime.now(clock);

        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId(), currentTime);

        validateOrderItems(orderSheet);

        validateItemCoupons(orderSheet, currentTime);

        validateCartCoupon(orderSheet, currentTime);

        validateUsedPoints(orderSheet);

        CreateOrderContext orderContext = orderContextFactory.create(orderSheet);

        Long orderId = orderCommandService.saveOrder(orderContext);

        return OrderCreateResult.of(orderId);
    }

    private void validateOrderItems(OrderSheet orderSheet) {
        List<Long> productVariantIds = orderSheet.getItems().stream()
                .map(OrderSheetItem::getProductVariantId).toList();
        OrderProductsResult products = orderProductPort.getProducts(productVariantIds);

        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        for (OrderSheetItem item : orderSheet.getItems()) {
            OrderProductsResult.OrderProductDetail product = productsMap.get(item.getProductVariantId());
            orderValidator.validateOrderable(product, item.getQuantity());
            item.validatePriceNotChanged(product.priceSnapshot());
        }
    }

    private void validateItemCoupons(OrderSheet orderSheet, LocalDateTime currentTime) {
        List<OrderSheetItem> appliedItemCouponItems = orderSheet.findOrderSheetItemsWithAppliedItemCoupon();

        if (appliedItemCouponItems.isEmpty()) {
            return;
        }

        List<Long> itemCouponIds = appliedItemCouponItems.stream().map(item -> item.getItemCouponSnapshot().getItemCouponId()).toList();
        ItemCouponsResult itemCoupons = orderCouponPort.getItemCoupons(orderSheet.getOrderer().getUserId(), itemCouponIds);
        Map<Long, ItemCouponsResult.ItemCouponResult> itemCouponsMap = itemCoupons.toMap();

        for (OrderSheetItem item : appliedItemCouponItems) {
            ItemCouponsResult.ItemCouponResult itemCoupon = itemCouponsMap.get(item.getItemCouponSnapshot().getItemCouponId());
            orderValidator.validateItemCoupon(itemCoupon, currentTime);
            item.validateItemCouponNotChanged(itemCoupon.itemCoupon());
        }
    }

    private void validateCartCoupon(OrderSheet orderSheet, LocalDateTime currentTime) {
        if (!orderSheet.hasCoupon()) {
            return;
        }

        Long cartCouponId = orderSheet.getCartCoupon().getCartCouponId();
        CartCouponResult cartCouponResult = orderCouponPort.getCartCoupon(orderSheet.getOrderer().getUserId(), cartCouponId);

        orderValidator.validateCartCoupon(cartCouponResult, currentTime);
        orderSheet.validateCartCouponNotChanged(cartCouponResult.cartCoupon());
    }

    private void validateUsedPoints(OrderSheet orderSheet) {
        if (orderSheet.getUsedPoints().equals(Money.ZERO)) {
            return;
        }

        OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(orderSheet.getOrderer().getUserId());

        orderValidator.validateAvailablePoints(ordererProfile.availablePoints(), orderSheet.getUsedPoints());
        orderSheet.validatePointsLimit(orderSheet.getUsedPoints(), pointUsagePolicy);
    }

    private OrderSheet getValidOrderSheet(Long orderSheetId, Long userId, LocalDateTime currentTime) {
        OrderSheet orderSheet = orderSheetRepository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(currentTime)) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }
}
