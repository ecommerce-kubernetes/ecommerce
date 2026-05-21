package com.example.order_service.ordersheet.application;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import com.example.order_service.ordersheet.application.external.OrderSheetCouponGateway;
import com.example.order_service.ordersheet.application.external.OrderSheetProductGateway;
import com.example.order_service.ordersheet.application.external.OrderSheetUserGateway;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.*;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import com.example.order_service.ordersheet.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 *  주문서(OrderSheet) Workflow 를 담당하는 애플리케이션 서비스
 *  <p>
 *      사용자의 최종 주문 전 까지의 주문서 상태를 관리
 *      외부 MSA 도메인과의 네트워크 통신을 통해 주문서를 관리
 *  </p>
 * @author 최민식
 * @since 2026. 05. 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetAppService {
    private final OrderSheetProperties orderSheetProperties;
    private final OrderSheetProductGateway orderSheetProductGateway;
    private final OrderSheetCouponGateway orderSheetCouponGateway;
    private final OrderSheetUserGateway orderSheetUserGateway;
    private final OrderSheetFactory factory;
    private final OrderSheetRepository repository;

    /**
     * 사용자 주문서 생성
     * <p>
     *     주문서를 생성하기 위해 상품, 쿠폰, 유저의 최신 상태를 스냅샷으로 저장
     * </p>
     * @param command 주문 대상 상품 및 초기 적용 쿠폰 목록
     * @return 생성 후 저장이 완료된 주문서의 정보(주문서 아이디, 만료 시간)
     */
    public OrderSheetResult.Create createOrderSheet(OrderSheetCommand.Create command) {
        OrderSheetUserResult.Profile userProfile = orderSheetUserGateway.getUserProfile(command.userId());
        OrderSheetProductResult.ProductList products = orderSheetProductGateway.getProducts(command.items());
        OrderSheetCouponResult.Calculate appliedCoupons = getAppliedCoupons(command, products);
        OrderSheet orderSheet = factory.createSheet(command, userProfile, products, appliedCoupons, orderSheetProperties.ttlMinutes());
        OrderSheet save = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetResult.Create.from(save);
    }

    /**
     * 사용자 주문서 조회
     * <p>
     *     유저 포인트 정보를 반환하기 위해 BFF 방식으로 유저 도메인에서 요청 유저의 포인트 정보를 조회하여 결과에 포함해 반환
     * </p>
     * @param sheetId 조회 주문서 아이디
     * @param userId 조회 유저 아이디
     * @return 저장된 주문서의 전체 정보(상품, 쿠폰, 배송 정보 등등)
     */
    public OrderSheetResult.Detail getOrderSheet(String sheetId, Long userId) {
        OrderSheet orderSheet = getValidateOrderSheet(sheetId, userId);
        OrderSheetUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(userId, orderSheet.getPointEligibleAmount());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    /**
     * 사용자 주문서 배송 정보 수정
     * <p>
     *     유저 포인트 정보를 반환하기 위해 BFF 방식으로 유저 도메인에서 요청 유저의 포인트 정보를 조회하여 결과에 포함해 반환
     * </p>
     * @param command 수정 배송 정보
     * @return 배송 정보가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult.Detail updateShippingAddress(OrderSheetCommand.UpdateShippingAddress command) {
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        ShippingAddress newAddress = factory.createShippingAddress(command);
        orderSheet.changeShippingAddress(newAddress);
        OrderSheetUserResult.UserPoint userPoints =
                orderSheetUserGateway.getUserPoints(command.userId(), orderSheet.getPointEligibleAmount());
        repository.save(orderSheet, orderSheet.getRemainingTtl());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    /**
     * 주문서 사용 포인트 변경
     * <p>
     *     주문서의 사용 포인트를 반영하고 주문서의 가격 정보를 적용 포인트에 맞추어 변경됨
     * </p>
     * @param command 변경 포인트 정보
     * @return 사용 포인트가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult.Detail updatePoints(OrderSheetCommand.UpdatePoints command) {
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        OrderSheetUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPointsForOrder(orderSheet.getOrderer().getUserId(),
                orderSheet.getPointEligibleAmount(), command.usedPoints());
        // [NOTE] 주문 가격 정보가 사용포인트에 맞추어 수정됨
        orderSheet.changeUsedPoints(command.usedPoints());
        repository.save(orderSheet, orderSheet.getRemainingTtl());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    /**
     * 주문서 상품 쿠폰 변경
     * <p>
     *     주문서 상품 쿠폰을 변경하고 변경된 쿠폰 정보에 맞추어 주문서의 가격 정보가 변경됨
     * </p>
     * @param command 변경 아이템 쿠폰 정보
     * @return 쿠폰 정보가 수정되어 저장이 완료된 주문서의 정보
     */
    public OrderSheetResult.Detail updateItemCoupon(OrderSheetCommand.UpdateItemCoupon command) {
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        OrderCouponSnapshot newCouponSnapshot = getNewItemCouponSnapshot(orderSheet, command.sheetItemId(), command.couponId());
        OrderSheetUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(orderSheet.getOrderer().getUserId(),
                orderSheet.getPointEligibleAmount());
        orderSheet.changeItemCoupon(command.sheetItemId(), newCouponSnapshot, userPoints.availablePoints());
        repository.save(orderSheet, orderSheet.getRemainingTtl());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    private OrderCouponSnapshot getNewItemCouponSnapshot(OrderSheet orderSheet, String sheetItemId, Long newCouponId) {
        OrderSheetItem sheetItem = orderSheet.getItem(sheetItemId);
        OrderSheetCouponResult.Calculate appliedCoupons =
                calculateCouponsForUpdate(orderSheet, sheetItemId, newCouponId, orderSheet.getCartCoupon().getCouponId());
        return factory.createItemCouponSnapshot(appliedCoupons, sheetItem.getProductSnapshot().getProductVariantId());
    }

    private OrderSheetCouponResult.Calculate calculateCouponsForUpdate(
            OrderSheet orderSheet,
            String sheetItemId,
            Long targetItemCouponId,
            Long targetCartCouponId
    ) {
        List<OrderSheetCommand.AppliedCouponItem> appliedItems = createAppliedCouponItems(orderSheet, sheetItemId, targetItemCouponId);
        boolean hasAnyItemCoupon = appliedItems.stream().anyMatch(item -> item.itemCouponId() != null);
        if (targetCartCouponId == null && !hasAnyItemCoupon) {
            return OrderSheetCouponResult.Calculate.empty();
        }
        OrderSheetCommand.CouponCalculate command = OrderSheetCommand.CouponCalculate.of(
                orderSheet.getOrderer().getUserId(), targetCartCouponId, appliedItems);
        return orderSheetCouponGateway.calculate(command);
    }

    private OrderSheet getValidateOrderSheet(String sheetId, Long userId) {
        OrderSheet orderSheet = repository.findById(sheetId)
                .orElseThrow(() -> new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NOT_FOUND));
        if (!orderSheet.isOwner(userId)) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }
        if (orderSheet.isExpired()) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }

    private List<OrderSheetCommand.AppliedCouponItem> createAppliedCouponItems(
            OrderSheet orderSheet,
            String sheetItemId,
            Long targetItemCouponId
    ) {
        return orderSheet.getItems().stream()
                .map(item -> {
                    Long couponId = item.getSheetItemId().equals(sheetItemId)
                            ? targetItemCouponId
                            : item.getItemCoupon().getCouponId();
                    return OrderSheetCommand.AppliedCouponItem.of(
                            item.getProductVariantId(),
                            item.getDiscountedPrice(),
                            item.getQuantity(),
                            couponId
                    );
                }).toList();
    }

    private OrderSheetCouponResult.Calculate getAppliedCoupons(OrderSheetCommand.Create command, OrderSheetProductResult.ProductList products) {
        if (!command.hasCoupons()) {
            return OrderSheetCouponResult.Calculate.empty();
        }
        Map<Long, OrderSheetProductResult.Info> productMap = products.getProductsMap();
        Map<Long, Long> couponMap = command.toCouponMap();
        OrderSheetCommand.CouponCalculate couponCommand = mapToCouponCommand(command, productMap, couponMap);
        return orderSheetCouponGateway.calculate(couponCommand);
    }

    private OrderSheetCommand.CouponCalculate mapToCouponCommand(OrderSheetCommand.Create command, Map<Long, OrderSheetProductResult.Info> productMap,
                                                                 Map<Long, Long> couponMap) {
        List<OrderSheetCommand.AppliedCouponItem> appliedCouponItems = command.items().stream().map(item -> {
            OrderSheetProductResult.Info product = productMap.get(item.productVariantId());
            Long itemCouponId = couponMap.get(item.productVariantId());
            return OrderSheetCommand.AppliedCouponItem.of(
                    item.productVariantId(),
                    product.discountedPrice(),
                    item.quantity(),
                    itemCouponId
            );
        }).toList();
        return OrderSheetCommand.CouponCalculate.of(command.userId(), command.cartCouponId(), appliedCouponItems);
    }
}
