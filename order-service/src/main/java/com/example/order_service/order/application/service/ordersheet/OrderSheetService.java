package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.*;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 주문서(OrderSheet) Workflow 를 담당하는 애플리케이션 서비스
 * <p>
 * 사용자의 최종 주문 전 까지의 주문서 상태를 관리
 * 외부 MSA 도메인과의 네트워크 통신을 통해 주문서를 관리
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetService {
    private final OrderSheetProperties orderSheetProperties;
    private final OrderProductGateway orderProductGateway;
    private final OrderCouponGateway orderCouponGateway;
    private final OrderUserGateway orderUserGateway;
    private final PointUsagePolicy pointUsagePolicy;
    private final OrderSheetRepository repository;
    private final Clock clock;


    public OrderSheetCreateResult createDirectOrderSheet(CreateDirectOrderSheetCommand command) {
        OrdererProfileResult ordererProfile = orderUserGateway.getOrdererProfile(command.userId());

        List<Long> orderVariantIds = command.toItemVariantIds();
        OrderProductResult products = orderProductGateway.getProducts(orderVariantIds);

        Map<Long, OrderProductResult.OrderProductDetail> productsMap = products.getProductsMap();
        List<OrderSheetItem> orderSheetItems = createOrderSheetItems(command, productsMap);

        OrderSheet orderSheet = createOrderSheet(ordererProfile, orderSheetItems);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetCreateResult.from(savedOrderSheet);
    }

    private OrderSheet createOrderSheet(OrdererProfileResult ordererProfile, List<OrderSheetItem> orderSheetItems) {
        OrderSheet orderSheet = OrderSheet.create(ordererProfile.orderer(), orderSheetItems,
                LocalDateTime.now(clock).plusMinutes(orderSheetProperties.ttlMinutes()));

        if (ordererProfile.defaultShippingAddress() != null) {
            orderSheet.changeShippingAddress(ordererProfile.defaultShippingAddress());
        }
        return orderSheet;
    }

    private List<OrderSheetItem> createOrderSheetItems(CreateDirectOrderSheetCommand command,
                                                       Map<Long, OrderProductResult.OrderProductDetail> productsMap) {
        return command.items().stream().map(orderVariant -> {
            OrderProductResult.OrderProductDetail product = productsMap.get(orderVariant.productVariantId());
            validateProductIsOrderable(product, orderVariant.quantity());
            return OrderSheetItem.create(product.productSnapshot(), product.priceSnapshot(),
                    orderVariant.quantity(), product.options());
        }).toList();
    }

    private void validateProductIsOrderable(OrderProductResult.OrderProductDetail product, int quantity) {
        if (product == null) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        if (product.status() != OrderProductStatus.ON_SALE) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
        }

        if (quantity > product.stock()) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
        }
    }

    public OrderSheetResult getOrderSheet(String orderSheetId, Long userId) {
        OrderSheet orderSheet = repository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }

        OrdererPointResult ordererPoints = orderUserGateway.getOrdererPoints(userId);
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);

        return OrderSheetResult.of(orderSheet, ordererPoints.availablePoints(), maxUsablePoints);
    }

    /**
     * 사용자 주문서 배송 정보 수정
     * <p>
     * 주문서의 배송 정보를 수정하고 수정된 결과를 반환한다
     * </p>
     *
     * @param command 수정 배송 정보
     * @return 배송 정보가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult updateShippingAddress(UpdateOrderSheetShippingAddressCommand command) {
        return null;
    }

    /**
     * 주문서 사용 포인트 변경
     * <p>
     * 주문서의 사용 포인트를 반영하고 주문서의 가격 정보를 적용 포인트에 맞추어 변경됨
     * </p>
     *
     * @param command 변경 포인트 정보
     * @return 사용 포인트가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult applyPoints(ApplyPointCommand command) {
        return null;
    }

    /**
     * 주문서 상품 쿠폰 변경
     * <p>
     * 주문서 상품 쿠폰을 변경하고 변경된 쿠폰 정보에 맞추어 주문서의 가격 정보가 변경됨
     * 쿠폰 변경으로 인해 주문서 적용 포인트가 사용 가능 포인트를 초과하는 경우 사용 가능 포인트로 주문서 적용 포인트가 조정됨
     * </p>
     *
     * @param command 변경 아이템 쿠폰 정보
     * @return 쿠폰 정보가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult applyItemCoupon(ApplyItemCouponCommand command) {
        return null;
    }

    private ItemCouponSnapshot getNewItemCouponSnapshot(OrderSheet orderSheet, String sheetItemId, Long newItemCouponId) {
        List<OrderCouponCommand.AppliedCouponItem> appliedItems = createAppliedItemsWithTarget(orderSheet, sheetItemId, newItemCouponId);
        OrderCouponResult.Calculate calculate =
                requestCouponCalculation(orderSheet.getOrderer().getUserId(), orderSheet.getCartCoupon().getCartCouponId(), appliedItems);
        OrderSheetItem sheetItem = orderSheet.getItem(sheetItemId);
        Map<Long, ItemCouponSnapshot> itemCouponMap = calculate.toItemCouponMap();
        return null;
    }

    private List<OrderCouponCommand.AppliedCouponItem> createAppliedItemsWithTarget(
            OrderSheet orderSheet, String targetSheetItemId, Long targetCouponId
    ) {
        return orderSheet.getItems().stream()
                .map(item -> {
                    Long couponId = item.getId().equals(targetSheetItemId) ? targetCouponId : item.getCouponId();
                    return OrderCouponCommand.AppliedCouponItem.of(
                            item.getProductVariantId(), item.getDiscountedPrice(), item.getQuantity(), couponId
                    );
                }).toList();
    }

    public OrderSheetResult applyCartCoupon(ApplyCartCouponCommand command) {
        return null;
    }

    private List<OrderCouponCommand.AppliedCouponItem> createCurrentAppliedItems(OrderSheet orderSheet) {
        return orderSheet.getItems().stream()
                .map(item -> OrderCouponCommand.AppliedCouponItem.of(
                        item.getProductVariantId(), item.getDiscountedPrice(), item.getQuantity(), item.getCouponId()
                )).toList();
    }

    private OrderCouponResult.Calculate requestCouponCalculation(
            Long userId, Long cartCouponId, List<OrderCouponCommand.AppliedCouponItem> appliedItems
    ) {
        boolean hasAnyItemCoupon = appliedItems.stream().anyMatch(item -> item.itemCouponId() != null);
        if (cartCouponId == null && !hasAnyItemCoupon) {
            return OrderCouponResult.Calculate.empty();
        }
        OrderCouponCommand.Calculate command = OrderCouponCommand.Calculate.of(userId, cartCouponId, appliedItems);
        return orderCouponGateway.calculate(command);
    }

    private OrderSheet getValidateOrderSheet(String sheetId, Long userId) {
        OrderSheet orderSheet = repository.findById(sheetId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        orderSheet.validateAccess(userId, LocalDateTime.now(clock));
        return orderSheet;
    }

    public OrderSheetCreateResult createCartOrderSheet(CreateCartOrderSheetCommand command) {
        return null;
    }
}
