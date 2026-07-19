package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResultDeprecate;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderSheetServiceTest {
    @InjectMocks
    private OrderSheetService orderSheetService;
    @Mock
    private OrderProductGateway orderProductGateway;
    @Mock
    private OrderCouponGateway orderCouponGateway;
    @Mock
    private OrderUserGateway orderUserGateway;
    @Mock
    private OrderSheetValidator validator;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L, BigDecimal.valueOf(0.1));
    @Spy
    private OrderSheetFactory factory = new OrderSheetFactory();
    @Spy
    private PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(properties);
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));


    @Nested
    @DisplayName("주문서 조회")
    class GetOrderSheet {

        @Test
        @DisplayName("주문서를 조회한다")
        void getOrderSheet() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            Money expectedAvailablePoints = orderSheet.calcAvailablePoints(point.ownedPoints(), pointUsagePolicy);
            given(repository.findById(anyString())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            //when
            OrderSheetResult result = orderSheetService.getOrderSheet(orderSheet.getId(), orderSheet.getOrderer().getUserId());
            //then
            assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
            assertThat(result.orderer().getUserId()).isEqualTo(orderSheet.getOrderer().getUserId());
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(expectedAvailablePoints);
        }

        @Test
        @DisplayName("주문서를 찾을 수 없는 경우 예외가 발생한다")
        void getOrderSheet_notFound() {
            //given
            String sheetId = "notFound";
            Long userId = 1L;
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.getOrderSheet(sheetId, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아닌 경우 예외가 발생한다")
        void getOrderSheet_not_match_ordererId() {
            //given
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheet.getId(), userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        @Test
        @DisplayName("주문서가 만료된 경우 예외가 발생한다")
        void getOrderSheet_expired() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now(clock).minusMinutes(20));
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheet.getId(), orderSheet.getOrderer().getUserId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_EXPIRED);
        }
    }

    @Nested
    @DisplayName("배송 정보 수정")
    class UpdateShippingAddress {

        @Test
        @DisplayName("배송 정보를 수정한다")
        void updateShippingAddress() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            String newPhone = "010-9876-5432";
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::receiverPhone), newPhone)
                    .create();
            OrderSheet orderSheet = createOrderSheet();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(any())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void updateShippingAddress_notFound() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then

        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updateShippingAddress_expired() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now(clock).minusMinutes(20));
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then

        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updateShippingAddress_no_permission() {
            //given
            String sheetId = "sheetId";
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then

        }
    }

    @Nested
    @DisplayName("사용 포인트 수정")
    class UpdatePoints {

        @Test
        @DisplayName("사용 포인트를 수정한다")
        void updatePoints() {
            //given
            Money usedPoints = Money.wons(100L);
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(usedPoints)
                    .build();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }

        @Test
        @DisplayName("사용 포인트가 주문에 적용할 수 있는 포인트를 초과하면 예외가 발생한다")
        void updatePoints_point_policy_violation() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(Money.wons(5000L))
                    .build();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            //when
            //then

        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void updatePoints_notFound() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then
        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updatePoints_no_permission() {
            //given
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then

        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updatePoints_expired() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now(clock).minusMinutes(20));
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 수정")
    class UpdateItemCoupon {

        @Test
        @DisplayName("상품 쿠폰을 해제한다")
        void updateItemCoupon_clear_coupon() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetItemId = orderSheet.getItems().get(0).getId();
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(orderSheet.getId(),
                    sheetItemId, orderSheet.getOrderer().getUserId(), null);
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .set(field(OrderCouponResult.Calculate::itemCoupons), List.of())
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }

        @Test
        @DisplayName("상품 쿠폰을 수정한다")
        void updateItemCoupon() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetItemId = orderSheet.getItems().get(0).getId();
            Long targetProductVariantId = orderSheet.getItems().get(0).getProductVariantId();
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(orderSheet.getId(),
                    sheetItemId, orderSheet.getOrderer().getUserId(), newCouponId);
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .generate(field(OrderCouponResult.Calculate::itemCoupons), gen -> gen.collection().size(1))
                    .set(field(OrderCouponResult.ItemCoupon::productVariantId), targetProductVariantId)
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then

        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class UpdateCartCoupon {

        @Test
        @DisplayName("장바구니 쿠폰 해제")
        void updateCartCoupon_clear_coupon() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(orderSheet.getId(),
                    orderSheet.getOrderer().getUserId(), null);
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .set(field(OrderCouponResult.Calculate::cartCoupon), null)
                    .set(field(OrderCouponResult.Calculate::itemCoupons), List.of())
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }

        @Test
        @DisplayName("장바구니 쿠폰을 수정한다")
        void updateCartCoupon_point_not_used() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(orderSheet.getId(),
                    orderSheet.getOrderer().getUserId(), newCouponId);
            CartCouponSnapshot cartCoupon = Instancio.of(CartCouponSnapshot.class)
                    .set(field(CartCouponSnapshot::getCartCouponId), newCouponId)
                    .create();
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .set(field(OrderCouponResult.Calculate::cartCoupon), cartCoupon)
                    .set(field(OrderCouponResult.Calculate::itemCoupons), List.of())
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }
    }

    private OrderSheet createOrderSheet() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderSheetItem sheetItem = OrderSheetItem.create( product, price, 1, options);
        return OrderSheet.create(orderer, List.of(sheetItem),LocalDateTime.now(clock).plusMinutes(30));
    }
}
