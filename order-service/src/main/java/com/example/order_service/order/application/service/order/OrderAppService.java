package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.order.application.dto.result.OrderResult;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.command.OrderProductCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.domain.model.OrderFailureCode;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderValidator validator;
    private final OrderMapper orderMapper;
    private final PointUsagePolicy pointPolicy;
    private final OrderUserGateway orderUserGateway;
    private final OrderProductGateway orderProductGateway;
    private final OrderCouponGateway orderCouponGateway;
    private final OrderSheetRepository orderSheetRepository;
    private final OrderService orderService;

    /**
     * 주문 생성
     * <p>
     * 주문서가 주문이 가능한 상태인지 검증 후 대기상태의 주문을 생성
     * </p>
     *
     * @param command 주문 생성 커맨드
     * @return 생성된 주문 정보
     */
    public OrderResult.Create initialOrder(OrderCommand.Create command) {
        OrderSheet orderSheet = findOrderSheetById(command.orderSheetId());
        if (!orderSheet.isOwner(command.userId())) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_ACCESS_DENIED);
        }
        if (orderSheet.isExpired()) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        OrderProductResult.ProductList products = getOrderedProducts(orderSheet.getItems());
        OrderCouponResult.Calculate appliedCoupons = getAppliedCoupons(orderSheet);
        OrderUserResult.UserPoint userPoints = getUserPoints(orderSheet);
        validator.validate(orderSheet, products, appliedCoupons, userPoints, pointPolicy);
        OrderContext.CreateOrderContext context = orderMapper.toContext(orderSheet);
        return orderService.saveOrder(context);
    }

    private OrderProductResult.ProductList getOrderedProducts(List<OrderSheetItem> items) {
        List<OrderProductCommand.OrderItem> command = items.stream().map(item ->
                OrderProductCommand.OrderItem.of(item.getProductVariantId(), item.getQuantity())).toList();
        return orderProductGateway.getProducts(command);
    }

    private OrderCouponResult.Calculate getAppliedCoupons(OrderSheet orderSheet) {
        if (!orderSheet.hasAnyCoupon()) {
            return OrderCouponResult.Calculate.empty();
        }
        List<OrderCouponCommand.AppliedCouponItem> itemCouponCommand = orderSheet.getItems().stream().map(
                item -> OrderCouponCommand.AppliedCouponItem.of(item.getProductVariantId(),
                        item.getDiscountedPrice(),
                        item.getQuantity(),
                        item.getCouponId())
        ).toList();
        OrderCouponCommand.Calculate command = OrderCouponCommand.Calculate
                .of(orderSheet.getOrderer().getUserId(),
                        orderSheet.getCartCoupon().getCouponId(),
                        itemCouponCommand);
        return orderCouponGateway.calculate(command);
    }

    private OrderUserResult.UserPoint getUserPoints(OrderSheet orderSheet) {
        Long userId = orderSheet.getOrderer().getUserId();
        Money usedPoints = orderSheet.getUsedPoints();
        return orderUserGateway.getUserPointsForOrder(userId, usedPoints);
    }

    public void preparePayment(String orderNo) {
    }

    public void processOrderFailure(String orderNo, OrderFailureCode orderFailureCode) {
    }

    public OrderDetailResponse confirmOrderPayment(String orderNo, Long userId, String paymentKey, Long amount) {
        return null;
    }

    private OrderSheet findOrderSheetById(String sheetId) {
        return orderSheetRepository.findById(sheetId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));
    }
}
