package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.command.OrderProductCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
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
import java.util.Optional;
import java.util.Set;

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
    private final OrderUserGateway orderSheetUserGateway;
    private final PointUsagePolicy pointUsagePolicy;
    private final OrderSheetFactory factory;
    private final OrderSheetRepository repository;
    private final Clock clock;

    /**
     * 사용자 주문서 생성
     * <p>
     * 주문서를 생성하기 위해 상품, 쿠폰, 유저의 최신 상태를 스냅샷으로 저장
     * </p>
     *
     * @param command 주문 대상 상품 및 초기 적용 쿠폰 목록
     * @return 생성 후 저장이 완료된 주문서의 정보(주문서 아이디, 만료 시간)
     */
    public OrderSheetResult.Create createOrderSheet(OrderSheetCommand.Create command) {
        OrderUserResult.Profile userProfile = orderSheetUserGateway.getUserProfile(command.userId());
        OrderProductResult.ProductList products = getOrderedProducts(command.items());
        validateForOrder(products, command);
        OrderCouponResult.Calculate appliedCoupons = getAppliedCoupons(command, products);
        OrderSheet orderSheet = factory.createSheet(command, userProfile, products, appliedCoupons, orderSheetProperties.ttlMinutes());
        OrderSheet save = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetResult.Create.from(save);
    }

    private OrderProductResult.ProductList getOrderedProducts(List<OrderSheetCommand.OrderItem> items) {
        List<OrderProductCommand.OrderItem> commands = items.stream().map(item ->
                        OrderProductCommand.OrderItem.of(item.productVariantId(), item.quantity())).toList();
        return orderProductGateway.getProductsForOrder(commands);
    }

    private void validateForOrder(OrderProductResult.ProductList productList, OrderSheetCommand.Create command) {
        Map<Long, Integer> reqItemMap = command.toQuantityMap();
        Map<Long, OrderProductResult.Info> productsMap = productList.getProductsMap();
        reqItemMap.forEach((reqId, reqQuantity) -> {
            OrderProductResult.Info product = Optional.ofNullable(productsMap.get(reqId))
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND));
            if (product.status() != OrderProductStatus.ON_SALE) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_ON_SALE);
            }
            if (reqQuantity > product.stock()) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
            }
        });
    }

    private OrderCouponResult.Calculate getAppliedCoupons(OrderSheetCommand.Create command,
                                                          OrderProductResult.ProductList products) {
        if (!command.hasCoupons()) {
            return OrderCouponResult.Calculate.empty();
        }
        Map<Long, OrderProductResult.Info> productMap = products.getProductsMap();
        Map<Long, Long> couponMap = command.toCouponMap();
        OrderCouponCommand.Calculate couponCommand = mapToCouponCommand(command, productMap, couponMap);
        return orderCouponGateway.calculate(couponCommand);
    }

    private OrderCouponCommand.Calculate mapToCouponCommand(OrderSheetCommand.Create command,
                                                                  Map<Long, OrderProductResult.Info> productMap,
                                                                  Map<Long, Long> couponMap) {
        List<OrderCouponCommand.AppliedCouponItem> appliedCouponItems = command.items().stream().map(item -> {
            OrderProductResult.Info product = productMap.get(item.productVariantId());
            Long itemCouponId = couponMap.get(item.productVariantId());
            return OrderCouponCommand.AppliedCouponItem.of(
                    item.productVariantId(),
                    product.priceSnapshot().getDiscountedPrice(),
                    item.quantity(),
                    itemCouponId
            );
        }).toList();
        return OrderCouponCommand.Calculate.of(command.userId(), command.cartCouponId(), appliedCouponItems);
    }

    /**
     * 사용자 주문서 조회
     * <p>
     * 주문 정보 및 보유 포인트와 주문에 사용할 수 있는 최대 포인트를 반환
     * </p>
     *
     * @param sheetId 조회 주문서 아이디
     * @param userId  조회 유저 아이디
     * @return 저장된 주문서의 전체 정보(상품, 쿠폰, 배송 정보 등등)
     */
    public OrderSheetResult.Detail getOrderSheet(String sheetId, Long userId) {
        OrderSheet orderSheet = getValidateOrderSheet(sheetId, userId);
        OrderUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(userId);
        Money availablePoints = orderSheet.calcAvailablePoints(userPoints.ownedPoints(), pointUsagePolicy);
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), availablePoints);
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
    public OrderSheetResult.Detail updateShippingAddress(OrderSheetCommand.UpdateShippingAddress command) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        ShippingAddress newAddress = ShippingAddress.of(command.receiverName(), command.receiverPhone(), command.zipCode(),
                command.address(), command.addressDetail());
        orderSheet.changeShippingAddress(newAddress);
        OrderUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(command.userId());
        Money availablePoints = orderSheet.calcAvailablePoints(userPoints.ownedPoints(), pointUsagePolicy);
        repository.save(orderSheet, orderSheet.getRemainingTtl(currentTime));
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), availablePoints);
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
    public OrderSheetResult.Detail updatePoints(OrderSheetCommand.UpdatePoints command) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        OrderUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(orderSheet.getOrderer().getUserId());
        // [NOTE] 주문 가격 정보가 사용포인트에 맞추어 수정됨
        orderSheet.changeUsedPoints(command.usedPoints(), userPoints.ownedPoints(), pointUsagePolicy);
        repository.save(orderSheet, orderSheet.getRemainingTtl(currentTime));
        Money availablePoints = orderSheet.calcAvailablePoints(userPoints.ownedPoints(), pointUsagePolicy);
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), availablePoints);
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
    public OrderSheetResult.Detail updateItemCoupon(OrderSheetCommand.UpdateItemCoupon command) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        OrderCouponSnapshot newCouponSnapshot = getNewItemCouponSnapshot(orderSheet, command.sheetItemId(), command.couponId());
        OrderUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(orderSheet.getOrderer().getUserId());
        // [NOTE] 상품 쿠폰 변경으로 인해 적용된 포인트가 사용 가능 포인트를 초과되는 경우 사용가능 포인트로 조정됨
        orderSheet.changeItemCoupon(command.sheetItemId(), newCouponSnapshot, userPoints.ownedPoints(), pointUsagePolicy);
        Money availablePoints = orderSheet.calcAvailablePoints(userPoints.ownedPoints(), pointUsagePolicy);
        repository.save(orderSheet, orderSheet.getRemainingTtl(currentTime));
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), availablePoints);
    }

    private OrderCouponSnapshot getNewItemCouponSnapshot(OrderSheet orderSheet, String sheetItemId, Long newItemCouponId) {
        List<OrderCouponCommand.AppliedCouponItem> appliedItems = createAppliedItemsWithTarget(orderSheet, sheetItemId, newItemCouponId);
        OrderCouponResult.Calculate calculate =
                requestCouponCalculation(orderSheet.getOrderer().getUserId(), orderSheet.getCartCoupon().getCouponId(), appliedItems);
        OrderSheetItem sheetItem = orderSheet.getItem(sheetItemId);
        Map<Long, OrderCouponSnapshot> itemCouponMap = calculate.toItemCouponMap();
        return itemCouponMap.getOrDefault(sheetItem.getProductVariantId(), OrderCouponSnapshot.empty());
    }

    private List<OrderCouponCommand.AppliedCouponItem> createAppliedItemsWithTarget(
            OrderSheet orderSheet, String targetSheetItemId, Long targetCouponId
    ) {
        return orderSheet.getItems().stream()
                .map(item -> {
                    Long couponId = item.getSheetItemId().equals(targetSheetItemId) ? targetCouponId : item.getCouponId();
                    return OrderCouponCommand.AppliedCouponItem.of(
                            item.getProductVariantId(), item.getDiscountedPrice(), item.getQuantity(), couponId
                    );
                }).toList();
    }

    /**
     * 주문서 장바구니 쿠폰 변경
     * <p>
     * 주문서 장바구니 쿠폰을 변경하고 변경된 쿠폰 정보에 맞추어 주문서 가격 정보가 변경됨
     * 장바구니 쿠폰 변경으로 인해 주문서에 적용된 포인트가 사용 가능 포인트를 초과되는 경우 사용 가능 포인트로 조정됨
     * </p>
     *
     * @param command 변경 장바구니 쿠폰 정보
     * @return 쿠폰 정보가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult.Detail updateCartCoupon(OrderSheetCommand.UpdateCartCoupon command) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        List<OrderCouponCommand.AppliedCouponItem> appliedItems = createCurrentAppliedItems(orderSheet);
        OrderCouponResult.Calculate calculate =
                requestCouponCalculation(orderSheet.getOrderer().getUserId(), command.couponId(), appliedItems);
        OrderCouponSnapshot newCartCouponSnapshot = calculate.cartCoupon() != null ? calculate.cartCoupon() : OrderCouponSnapshot.empty();
        OrderUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(orderSheet.getOrderer().getUserId());
        // [NOTE] 장바구니 쿠폰 변경으로 인해 적용된 포인트가 사용 가능 포인트를 초과되는 경우 사용 가능 포인트로 조정됨
        orderSheet.changeCartCoupon(newCartCouponSnapshot, userPoints.ownedPoints(), pointUsagePolicy);
        Money availablePoints = orderSheet.calcAvailablePoints(userPoints.ownedPoints(), pointUsagePolicy);
        repository.save(orderSheet, orderSheet.getRemainingTtl(currentTime));
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), availablePoints);
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
}
