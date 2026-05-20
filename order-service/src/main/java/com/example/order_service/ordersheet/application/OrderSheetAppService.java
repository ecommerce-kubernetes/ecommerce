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
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        List<OrderSheetProductResult.Info> products = orderSheetProductGateway.getProducts(command.items());
        OrderSheetCouponResult.Calculate appliedCoupons = getAppliedCoupons(command, products);
        List<OrderSheetItem> orderSheetItems = mapToOrderSheetItems(command, products, appliedCoupons);
        OrderSheet orderSheet = createOrderSheet(userProfile, orderSheetItems, appliedCoupons.cartCoupon());
        OrderSheet save = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetResult.Create.from(save);
    }

    // 주문서 조회
    public OrderSheetResult.Detail getOrderSheet(String sheetId, Long userId) {
        // 주문서 조회
        OrderSheet orderSheet = getValidateOrderSheet(sheetId, userId);
        // 유저 포인트 조회
        OrderSheetUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(userId, orderSheet.getPointEligibleAmount());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    // 배송 정보 수정
    public OrderSheetResult.Detail updateShippingAddress(OrderSheetCommand.UpdateShippingAddress command) {
        // 주문서 조회
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        // 새 배송 정보 생성
        ShippingAddress newAddress = ShippingAddress.of(command.receiverName(), command.receiverPhone(), command.zipCode(), command.address(), command.addressDetail());
        // 배송 정보 변경
        orderSheet.changeShippingAddress(newAddress);
        // 유저 포인트 조회
        OrderSheetUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPoints(command.userId(), orderSheet.getPointEligibleAmount());
        // 변경 사항 저장
        repository.save(orderSheet, orderSheet.getRemainingTtl());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    // 사용 포인트 수정
    public OrderSheetResult.Detail updatePoints(OrderSheetCommand.UpdatePoints command) {
        // 주문서 조회
        OrderSheet orderSheet = getValidateOrderSheet(command.sheetId(), command.userId());
        // 유저 포인트 검증
        OrderSheetUserResult.UserPoint userPoints = orderSheetUserGateway.getUserPointsForOrder(orderSheet.getOrderer().getUserId(),
                orderSheet.getPointEligibleAmount(), command.usedPoints());
        // 포인트 수정
        orderSheet.changeUsedPoints(command.usedPoints());
        // 변경 사항 저장
        repository.save(orderSheet, orderSheet.getRemainingTtl());
        return OrderSheetResult.Detail.of(orderSheet, userPoints.ownedPoints(), userPoints.availablePoints());
    }

    // 상품 쿠폰 수정
    public OrderSheetResult.Detail updateItemCoupon(OrderSheetCommand.UpdateItemCoupon command) {
        // 주문서 조회
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
        OrderSheetCouponResult.Calculate appliedCoupons = calculateCouponsForUpdate(orderSheet, sheetItemId, newCouponId, orderSheet.getCartCoupon().getCouponId());
        Map<Long, OrderSheetCouponResult.ItemCoupon> couponMap = appliedCoupons.toItemCouponMap();
        return Optional.ofNullable(couponMap.get(sheetItem.getProductSnapshot().getProductVariantId()))
                .map(coupon -> OrderCouponSnapshot.of(coupon.couponId(), coupon.couponName(), coupon.discountAmount()))
                .orElseGet(OrderCouponSnapshot::empty);
    }

    private OrderSheetCouponResult.Calculate calculateCouponsForUpdate(
            OrderSheet orderSheet,
            String sheetItemId,
            Long targetItemCouponId,
            Long targetCartCouponId
    ) {
        List<OrderSheetCommand.AppliedCouponItem> appliedItems = orderSheet.getItems().stream().map(item -> {
            Long couponId = item.getSheetItemId().equals(sheetItemId) ? targetItemCouponId : item.getItemCoupon().getCouponId();
            return OrderSheetCommand.AppliedCouponItem.of(
                    item.getProductSnapshot().getProductVariantId(),
                    item.getItemPrice().getDiscountedPrice(),
                    item.getQuantity(),
                    couponId
            );
        }).toList();
        boolean hasAnyItemCoupon = appliedItems.stream().anyMatch(item -> item.itemCouponId() != null);
        if (targetCartCouponId == null && !hasAnyItemCoupon) {
            return OrderSheetCouponResult.Calculate.empty();
        }
        OrderSheetCommand.CouponCalculate command = OrderSheetCommand.CouponCalculate.of(
                orderSheet.getOrderer().getUserId(),
                targetCartCouponId,
                appliedItems);
        return orderSheetCouponGateway.calculate(command);
    }

    // 주문서 검증
    private OrderSheet getValidateOrderSheet(String sheetId, Long userId) {
        //주문서 조회
        OrderSheet orderSheet = repository.findById(sheetId)
                .orElseThrow(() -> new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NOT_FOUND));
        // 주문자 검증
        if (!orderSheet.isOwner(userId)) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }
        // 주문서 만료 여부 검증
        if (orderSheet.isExpired()) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }

    // 주문 시트 도메인 생성
    private OrderSheet createOrderSheet(OrderSheetUserResult.Profile profile, List<OrderSheetItem> items, OrderSheetCouponResult.CartCoupon cartCoupon) {
        OrderCouponSnapshot cartCouponSnapshot = Optional.ofNullable(cartCoupon)
                .map(coupon -> OrderCouponSnapshot.of(coupon.couponId(), coupon.couponName(), coupon.discountAmount()))
                .orElseGet(OrderCouponSnapshot::empty);
        Orderer orderer = Orderer.of(profile.userId(), profile.userName(), profile.phoneNumber());
        ShippingAddress shippingAddress = ShippingAddress.of(profile.shippingAddress().receiverName(),
                profile.shippingAddress().receiverPhone(), profile.shippingAddress().zipCode(), profile.shippingAddress().address(), profile.shippingAddress().addressDetail());
        return OrderSheet.create(generateId(), orderer, shippingAddress, items, cartCouponSnapshot, LocalDateTime.now(), orderSheetProperties.ttlMinutes());
    }

    //적용 쿠폰 조회
    private OrderSheetCouponResult.Calculate getAppliedCoupons(OrderSheetCommand.Create command, List<OrderSheetProductResult.Info> products) {
        if (!command.hasCoupons()) {
            return OrderSheetCouponResult.Calculate.empty();
        }
        Map<Long, OrderSheetProductResult.Info> productMap = products.stream()
                .collect(Collectors.toMap(OrderSheetProductResult.Info::productVariantId, Function.identity()));
        Map<Long, Long> couponMap = command.toCouponMap();
        OrderSheetCommand.CouponCalculate couponCommand = mapToCouponCommand(command, productMap, couponMap);
        return orderSheetCouponGateway.calculate(couponCommand);
    }

    //쿠폰 커맨드 매핑
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

    //주문 시트 아이템 매핑
    private List<OrderSheetItem> mapToOrderSheetItems(OrderSheetCommand.Create command, List<OrderSheetProductResult.Info> products, OrderSheetCouponResult.Calculate coupon) {
        Map<Long, OrderSheetProductResult.Info> productMap = products.stream().collect(Collectors.toMap(OrderSheetProductResult.Info::productVariantId, Function.identity()));
        Map<Long, OrderSheetCouponResult.ItemCoupon> couponMap = coupon.toItemCouponMap();
        return command.items().stream()
                .map(orderItem -> createOrderSheetItem(orderItem, productMap, couponMap)).toList();
    }

    //주문 시트 아이템 생성
    private OrderSheetItem createOrderSheetItem(OrderSheetCommand.OrderItem orderItem,
                                                Map<Long, OrderSheetProductResult.Info> productMap,
                                                Map<Long, OrderSheetCouponResult.ItemCoupon> couponMap) {
        OrderSheetProductResult.Info product = productMap.get(orderItem.productVariantId());
        OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(), product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
        OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(product.originalPrice(), product.discountRate(), product.discountAmount(), product.discountedPrice());
        List<OrderSheetItemOptionSnapshot> optionSnapshots = mapToOptionSnapshots(product.options());
        OrderCouponSnapshot couponSnapshot = Optional.ofNullable(couponMap.get(orderItem.productVariantId()))
                .map(itemCoupon -> OrderCouponSnapshot.of(itemCoupon.couponId(), itemCoupon.couponName(), itemCoupon.discountAmount()))
                .orElseGet(OrderCouponSnapshot::empty);
        String sheetItemId = generateId();
        return OrderSheetItem.create(sheetItemId, productSnapshot, priceSnapshot, couponSnapshot, orderItem.quantity(), optionSnapshots);
    }

    //상품 옵션 매핑
    private List<OrderSheetItemOptionSnapshot> mapToOptionSnapshots(List<OrderSheetProductResult.Option> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream().map(option ->
                OrderSheetItemOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
