package com.example.order_service.order.application;

import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.ErrorCode;
import com.example.order_service.common.util.AsyncUtil;
import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.application.dto.OrderPreparationData;
import com.example.order_service.order.application.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.dto.command.CreateOrderItemCommand;
import com.example.order_service.order.application.dto.command.OrderCommand;
import com.example.order_service.order.application.dto.result.*;
import com.example.order_service.order.application.event.*;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderPaymentGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.domain.model.OrderFailureCode;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.model.vo.PaymentStatus;
import com.example.order_service.order.domain.model.vo.ProductStatus;
import com.example.order_service.order.domain.service.OrderPriceCalculator;
import com.example.order_service.order.domain.service.OrderService;
import com.example.order_service.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.order.domain.service.dto.result.CalculatedOrderAmounts;
import com.example.order_service.order.domain.service.dto.result.OrderDto;
import com.example.order_service.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.exception.PaymentErrorCode;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderPaymentGateway orderPaymentGateway;
    private final OrderUserGateway orderUserGateway;
    private final Executor applicationTaskExecutor;
    private final OrderProductGateway orderProductGateway;
    private final OrderCouponGateway orderCouponGateway;
    private final OrderPriceCalculator calculator;
    private final OrderCreationContextMapper mapper;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderResult.Create initialOrder(OrderCommand.Create command) {
        return null;
    }

    public CreateOrderResponse initialOrder(CreateOrderCommand command){
        // 중복 상품이 있는지 검증
        validateUniqueItems(command.getOrderItemCommands());
        //CompletableFuture 을 사용해서 상품, 유저 요청을 비동기로 동시에 조회
        OrderPreparationData orderPreparationData = getOrderPreparationData(command);

        if (orderPreparationData.getUser().availablePoints() < command.getPointToUse()){
            throw new BusinessException(OrderErrorCode.ORDER_USER_INSUFFICIENT_POINT_BALANCE);
        }

        if (orderPreparationData.getProducts().size() != command.getProductVariantIds().size()) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        for(OrderProductResult.Info product: orderPreparationData.getProducts()) {
            Map<Long, Integer> quantityMap = command.getQuantityMap();
            if (!product.status().equals(ProductStatus.ORDERABLE)) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_ON_SALE);
            }
            if (quantityMap.get(product.productVariantId()) > product.stock()) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
            }
        }

        //주문 상품 가격 정보 계산
        OrderProductAmount productAmount = calculator.calculateItemAmounts(command.getOrderItemCommands(), orderPreparationData.getProducts());
        OrderCouponResult.CouponValidation coupon = orderCouponGateway.calculateCouponDiscount(command.getUserId(), command.getCouponId(), productAmount.getSubTotalAmount());
        //할인 적용 최종 금액 계산
        CalculatedOrderAmounts calculatedOrderAmounts = calculator.calculateOrderPrice(productAmount, coupon, command.getPointToUse(), command.getExpectedPrice());
        // 주문 생성 Context 매핑
        OrderCreationContext creationContext =
                mapper.mapOrderCreationContext(orderPreparationData.getUser(), calculatedOrderAmounts, coupon, command, orderPreparationData.getProducts());
        // 주문 저장
        OrderDto orderDto = orderService.saveOrder(creationContext);
        //SAGA 진행을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderCreatedEvent.from(orderDto));
        return CreateOrderResponse.from(orderDto);
    }

    public void preparePayment(String orderNo) {
        OrderDto orderDto = orderService.preparePaymentWaiting(orderNo);
        // SSE 메시지 전송을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderPaymentReadyEvent.from(orderDto));
    }

    public void processOrderFailure(String orderNo, OrderFailureCode orderFailureCode){
        OrderDto orderDto = orderService.canceledOrder(orderNo, orderFailureCode);
        // SSE 메시지 전송을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderFailedEvent.from(orderDto));
    }

    public OrderDetailResponse confirmOrderPayment(String orderNo, Long userId, String paymentKey, Long amount) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        // 결제 가능한 상태인지 검증
        validBeforePayment(order, amount);
        // 결제 서비스 결제 승인 요청
        OrderPaymentResult.Payment orderPaymentInfo = confirmPayment(order.getOrderNo(), paymentKey, order.getOrderPriceInfo().getFinalPaymentAmount());
        if (orderPaymentInfo.status() != PaymentStatus.DONE) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_APPROVAL_FAIL);
        }
        PaymentCreationContext paymentContext = mapper.mapPaymentCreationContext(orderPaymentInfo);
        OrderDto orderDto = orderService.completePayment(paymentContext);
        List<Long> orderedItemVariantIds = orderDto.getOrderItems().stream().map(i -> i.getOrderedProduct().getProductVariantId()).toList();
        eventPublisher.publishEvent(PaymentCompletedEvent.of(orderDto.getOrderNo(), orderDto.getOrderer().getUserId(), orderedItemVariantIds));
        return OrderDetailResponse.from(orderDto);
    }

    public OrderDetailResponse getOrder(Long userId, String orderNo) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        return OrderDetailResponse.from(order);
    }

    public PageDto<OrderListResponse> getOrders(Long userId, OrderSearchCondition condition){
        Page<OrderDto> orders = orderService.getOrders(userId, condition);
        return PageDto.of(orders, OrderListResponse::from);
    }

    private OrderPaymentResult.Payment confirmPayment(String orderNo, String paymentKey, Long amount) {
        try {
            // 결제 서비스를 호출해 결제를 진행
            return orderPaymentGateway.confirmOrderPayment(orderNo, paymentKey, amount);
        } catch (BusinessException e) {
            // 결제 서비스 호출중 예외 발생시 주문 상태를 변경하고 saga 롤백을 위한 이벤트를 발행
            OrderFailureCode orderFailureCode = mapToOrderFailureCode(e.getErrorCode());
            OrderDto failOrderDto = orderService.failPayment(orderNo, orderFailureCode);
            eventPublisher.publishEvent(PaymentFailedEvent
                    .of(failOrderDto.getOrderNo(),
                            failOrderDto.getOrderer().getUserId(),
                            e.getErrorCode().name(),
                            e.getErrorCode().getMessage()));
            throw e;
        }
    }

    private void validBeforePayment(OrderDto order, Long amount) {
        if (!order.getStatus().equals(OrderStatus.PAYMENT_WAITING)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        if (order.getOrderPriceInfo().getFinalPaymentAmount() != amount) {
            throw new BusinessException(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }
    }

    private OrderFailureCode mapToOrderFailureCode(ErrorCode errorCode) {
        if (errorCode == PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE) {
            return OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_TIMEOUT) {
            return OrderFailureCode.PAYMENT_TIMEOUT;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_NOT_FOUND) {
            return OrderFailureCode.PAYMENT_NOT_FOUND;
        }
        return OrderFailureCode.UNKNOWN;
    }

    private void validateUniqueItems(List<CreateOrderItemCommand> items) {
        Set<Long> setIds = items.stream().map(CreateOrderItemCommand::getProductVariantId).collect(Collectors.toSet());
        if (items.size() != setIds.size()) {
            throw new BusinessException(OrderErrorCode.ORDER_DUPLICATE_ORDER_PRODUCT);
        }
    }

    // 유저정보, 상품 정보를 비동기로 동시 조회
    private OrderPreparationData getOrderPreparationData(CreateOrderCommand command) {
        Executor contextAwareExecutor = task -> {
            ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();
            applicationTaskExecutor.execute(snapshot.wrap(task));
        };

        CompletableFuture<OrderUserResult.OrdererInfo> userFuture = CompletableFuture.supplyAsync(
                () -> orderUserGateway.getUser(command.getUserId()),
                contextAwareExecutor
        );

        CompletableFuture<List<OrderProductResult.Info>> productFuture = CompletableFuture.supplyAsync(
                () -> orderProductGateway.getProducts(command.getProductVariantIds()),
                contextAwareExecutor
        );
        CompletableFuture.allOf(userFuture, productFuture).join();

        return OrderPreparationData.builder()
                .user(AsyncUtil.join(userFuture))
                .products(AsyncUtil.join(productFuture))
                .build();
    }
}
