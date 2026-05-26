package com.example.order_service.order.application;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.dto.result.*;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.exception.OrderSheetErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderSheetAppServiceTest {
    @InjectMocks
    private OrderSheetAppService orderSheetAppService;
    @Mock
    private OrderProductGateway orderProductGateway;
    @Mock
    private OrderCouponGateway orderCouponGateway;
    @Mock
    private OrderUserGateway orderUserGateway;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L, BigDecimal.valueOf(0.1));
    @Spy
    private OrderSheetFactory factory = new OrderSheetFactory();

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("쿠폰을 적용한 경우 쿠폰 정보를 조회하고 주문서를 생성한다")
        void createOrderSheet_coupon_applied(){
            //given
            OrderSheetCommand.Create command = createCouponAppliedCommand();
            OrderProductResult.ProductList products = createProducts();
            OrderCouponResult.Calculate coupon = createCoupon();
            OrderUserResult.Profile userProfile = createUserProfile();
            given(orderUserGateway.getUserProfile(any())).willReturn(userProfile);
            given(orderProductGateway.getProducts(anyList())).willReturn(products);
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderProductGateway).getProducts(anyList());
            verify(orderCouponGateway).calculate(any());
        }

        @Test
        @DisplayName("쿠폰을 적용하지 않은 경우 쿠폰 정보를 조회하지 않고 주문서를 생성한다")
        void createOrderSheet_coupon_not_applied(){
            //given
            OrderSheetCommand.Create command = createNotCouponAppliedCommand();
            OrderProductResult.ProductList products = createProducts();
            OrderUserResult.Profile userProfile = createUserProfile();
            given(orderUserGateway.getUserProfile(any())).willReturn(userProfile);
            given(orderProductGateway.getProducts(anyList())).willReturn(products);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderProductGateway).getProducts(anyList());
            verify(orderCouponGateway, never()).calculate(any());
        }

        private OrderSheetCommand.Create createCouponAppliedCommand() {
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.ItemCoupon itemCoupon = OrderSheetCommand.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(1L)
                    .build();

            return OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .cartCouponId(2L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }

        private OrderSheetCommand.Create createNotCouponAppliedCommand() {
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            return OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .cartCouponId(null)
                    .itemCoupons(List.of())
                    .build();
        }

        private OrderUserResult.Profile createUserProfile() {
            OrderUserResult.ShippingAddress shippingAddress = OrderUserResult.ShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();
            return OrderUserResult.Profile.builder()
                    .userId(1L)
                    .userName("주문자")
                    .phoneNumber("010-1234-5678")
                    .shippingAddress(shippingAddress)
                    .build();
        }

        private OrderProductResult.ProductList createProducts() {
            OrderProductResult.Option size = OrderProductResult.Option.builder()
                    .optionTypeName("사이즈")
                    .optionValueName("XL")
                    .build();
            OrderProductResult.Option blue = OrderProductResult.Option.builder()
                    .optionTypeName("색상")
                    .optionValueName("BLUE")
                    .build();
            OrderProductResult.Info product = OrderProductResult.Info.builder()
                    .productId(1L)
                    .productVariantId(1L)
                    .sku("PROD-XL-BLUE")
                    .productName("청바지")
                    .originalPrice(Money.wons(10000L))
                    .discountRate(10)
                    .discountAmount(Money.wons(1000L))
                    .discountedPrice(Money.wons(9000L))
                    .thumbnail("/product/product/jean_1.jpg")
                    .options(List.of(size, blue))
                    .build();
            return OrderProductResult.ProductList.builder()
                    .products(List.of(product))
                    .build();
        }

        private OrderCouponResult.Calculate createCoupon() {
            OrderCouponResult.CartCoupon cartCoupon = OrderCouponResult.CartCoupon.builder()
                    .couponId(1L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            OrderCouponResult.ItemCoupon itemCoupon = OrderCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(2L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            return OrderCouponResult.Calculate
                    .builder()
                    .cartCoupon(cartCoupon)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }
    }

    @Nested
    @DisplayName("주문서 조회")
    class GetOrderSheet {

        @Test
        @DisplayName("주문서를 조회한다")
        void getOrderSheet(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderUserResult.UserPoint point = OrderUserResult.UserPoint.builder()
                    .userId(1L)
                    .ownedPoints(Money.wons(10000L))
                    .build();

            given(repository.findById(anyString())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            //when
            OrderSheetResult.Detail result = orderSheetAppService.getOrderSheet("sheetId", 1L);
            //then
            assertThat(result.sheetId()).isEqualTo("sheetId");
            assertThat(result.orderer().userId()).isEqualTo(1L);
            assertThat(result.point().availablePoints()).isEqualTo(Money.wons(10000L));
        }

        @Test
        @DisplayName("주문서를 찾을 수 없는 경우 예외가 발생한다")
        void getOrderSheet_notFound(){
            //given
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.getOrderSheet("unKnown", 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아닌 경우 예외가 발생한다")
        void getOrderSheet_not_match_ordererId(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.getOrderSheet("sheetId", 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }
    }

    @Nested
    @DisplayName("배송 정보 수정")
    class UpdateShippingAddress {

        @Test
        @DisplayName("배송 정보를 수정한다")
        void updateShippingAddress(){
            //given
            String sheetId = "sheetId";
            Long userId = 1L;

            OrderSheetCommand.UpdateShippingAddress command = OrderSheetCommand.UpdateShippingAddress.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .receiverName("새 수령인")
                    .receiverPhone("010-9876-5432")
                    .zipCode("54321")
                    .address("서울시 테헤란로 321")
                    .addressDetail("321동 4321호")
                    .build();
            OrderSheet orderSheet = createOrderSheet();
            OrderUserResult.UserPoint point = OrderUserResult.UserPoint.builder()
                    .userId(1L)
                    .ownedPoints(Money.wons(10000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(any())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updateShippingAddress(command);
            //then
            assertThat(result.shippingAddress())
                    .extracting("receiverName", "receiverPhone", "zipCode", "address", "addressDetail")
                    .containsExactlyInAnyOrder(
                            "새 수령인",
                            "010-9876-5432",
                            "54321",
                            "서울시 테헤란로 321",
                            "321동 4321호"
                    );

            ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(repository, times(1)).save(eq(orderSheet), durationCaptor.capture());
            Duration capturedDuration = durationCaptor.getValue();
            assertThat(capturedDuration.toMinutes()).isBetween(29L, 30L);
        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void updateShippingAddress_notFound(){
            //given
            String sheetId = "sheetId";
            Long userId = 1L;

            OrderSheetCommand.UpdateShippingAddress command = OrderSheetCommand.UpdateShippingAddress.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .receiverName("새 수령인")
                    .receiverPhone("010-9876-5432")
                    .zipCode("54321")
                    .address("서울시 테헤란로 321")
                    .addressDetail("321동 4321호")
                    .build();
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.updateShippingAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_NOT_FOUND);
        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updateShippingAddress_expired(){
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
            OrderSheetCommand.UpdateShippingAddress command = OrderSheetCommand.UpdateShippingAddress.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .receiverName("새 수령인")
                    .receiverPhone("010-9876-5432")
                    .zipCode("54321")
                    .address("서울시 테헤란로 321")
                    .addressDetail("321동 4321호")
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.updateShippingAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_EXPIRED);
        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updateShippingAddress_no_permission(){
            //given
            String sheetId = "sheetId";
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdateShippingAddress command = OrderSheetCommand.UpdateShippingAddress.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .receiverName("새 수령인")
                    .receiverPhone("010-9876-5432")
                    .zipCode("54321")
                    .address("서울시 테헤란로 321")
                    .addressDetail("321동 4321호")
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.updateShippingAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }
    }

    @Nested
    @DisplayName("사용 포인트 수정")
    class UpdatePoints {

        @Test
        @DisplayName("사용 포인트를 수정한다")
        void updatePoints() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            OrderUserResult.UserPoint point = OrderUserResult.UserPoint.builder()
                    .userId(1L)
                    .ownedPoints(Money.wons(10000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPointsForOrder(anyLong(), any())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updatePoints(command);
            //then
            assertThat(result.point().usedPoints()).isEqualTo(Money.wons(2000L));
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(Money.wons(2000L));
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(Money.wons(5000L));
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
            assertThatThrownBy(() -> orderSheetAppService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updatePoints_no_permission() {
            //given
            String sheetId = "sheetId";
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updatePoints_expired() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_EXPIRED);
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 수정")
    class UpdateItemCoupon {

        @Test
        @DisplayName("상품 쿠폰을 해제한다")
        void updateItemCoupon_clear_coupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetId = "sheetId";
            String sheetItemId = "sheetItemId";
            Long userId = 1L;
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(sheetId, sheetItemId, userId, null);
            OrderCouponResult.Calculate couponResult = createCouponNotUsedResult();
            OrderUserResult.UserPoint pointResult = createUserResult();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderUserGateway.getUserPoints(any())).willReturn(pointResult);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updateItemCoupon(command);
            //then
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(Money.wons(1000L));
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(Money.ZERO);
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(Money.wons(8000L));
        }

        @Test
        @DisplayName("상품 쿠폰을 수정한다")
        void updateItemCoupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            orderSheet.changeUsedPoints(Money.wons(1000L));
            String sheetId = "sheetId";
            String sheetItemId = "sheetItemId";
            Long userId = 1L;
            Long couponId = 10L;
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(sheetId, sheetItemId, userId, couponId);
            OrderCouponResult.Calculate couponResult = createCouponResult();
            OrderUserResult.UserPoint pointResult = createUserResult();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderUserGateway.getUserPoints(any())).willReturn(pointResult);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updateItemCoupon(command);
            //then
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(Money.wons(3000L));
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(Money.wons(500L));
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(Money.wons(5500L));
        }

        private OrderCouponResult.Calculate createCouponResult() {
            OrderCouponResult.CartCoupon cartCoupon = OrderCouponResult.CartCoupon.builder()
                    .couponId(2L)
                    .couponName("첫구매 1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            OrderCouponResult.ItemCoupon itemCoupon = OrderCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(10L)
                    .couponName("전 품목 2000원 할인 쿠폰")
                    .discountAmount(Money.wons(2000L))
                    .build();
            return OrderCouponResult.Calculate.builder()
                    .cartCoupon(cartCoupon)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }

        private OrderCouponResult.Calculate createCouponNotUsedResult() {
            OrderCouponResult.CartCoupon cartCoupon = OrderCouponResult.CartCoupon.builder()
                    .couponId(2L)
                    .couponName("첫구매 1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            return OrderCouponResult.Calculate.builder()
                    .cartCoupon(cartCoupon)
                    .itemCoupons(List.of())
                    .build();
        }

        private OrderUserResult.UserPoint createUserResult() {
            return OrderUserResult.UserPoint.builder()
                    .userId(1L)
                    .ownedPoints(Money.wons(10000L))
                    .build();
        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class UpdateCartCoupon {

        @Test
        @DisplayName("장바구니 쿠폰 해제")
        void updateCartCoupon_clear_coupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(sheetId, userId, null);
            OrderCouponResult.Calculate couponResult = createCartCouponNotUsedResult();
            OrderUserResult.UserPoint userResult = createUserResult();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderUserGateway.getUserPoints(any())).willReturn(userResult);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updateCartCoupon(command);
            //then
            assertThat(result.cartCoupon().couponId()).isEqualTo(null);
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(Money.wons(1000L));
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(Money.ZERO);
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(Money.wons(8000L));
        }

        @Test
        @DisplayName("장바구니 쿠폰을 수정한다 (포인트 미사용)")
        void updateCartCoupon_point_not_used(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetId = "sheetId";
            Long userId = 1L;
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(sheetId, userId, newCouponId);
            OrderCouponResult.Calculate couponResult = createCouponResult();
            OrderUserResult.UserPoint userResult = createUserResult();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderUserGateway.getUserPoints(any())).willReturn(userResult);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updateCartCoupon(command);
            //then
            assertThat(result.cartCoupon().couponId()).isEqualTo(10L);
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(Money.wons(3000L));
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(Money.ZERO);
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(Money.wons(6000L));
        }

        @Test
        @DisplayName("장바구니 쿠폰을 수정한다 (포인트 조정)")
        void updateCartCoupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            orderSheet.changeUsedPoints(Money.wons(1000L));
            String sheetId = "sheetId";
            Long userId = 1L;
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(sheetId, userId, newCouponId);
            OrderCouponResult.Calculate couponResult = createCouponResult();
            OrderUserResult.UserPoint userResult = createUserResult();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderUserGateway.getUserPoints(any())).willReturn(userResult);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetAppService.updateCartCoupon(command);
            //then
            assertThat(result.cartCoupon().couponId()).isEqualTo(10L);
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(Money.wons(3000L));
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(Money.wons(500L));
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(Money.wons(5500L));
        }

        private OrderCouponResult.Calculate createCartCouponNotUsedResult() {
            OrderCouponResult.ItemCoupon itemCoupon = OrderCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(1L)
                    .couponName("하의 1000원 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            return OrderCouponResult.Calculate.builder()
                    .cartCoupon(null)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }

        private OrderCouponResult.Calculate createCouponResult() {
            OrderCouponResult.CartCoupon cartCoupon = OrderCouponResult.CartCoupon.builder()
                    .couponId(10L)
                    .couponName("첫구매 2000원 할인 쿠폰")
                    .discountAmount(Money.wons(2000L))
                    .build();
            OrderCouponResult.ItemCoupon itemCoupon = OrderCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(1L)
                    .couponName("하의 1000원 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            return OrderCouponResult.Calculate.builder()
                    .cartCoupon(cartCoupon)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }

        private OrderUserResult.UserPoint createUserResult() {
            return OrderUserResult.UserPoint.builder()
                    .userId(1L)
                    .ownedPoints(Money.wons(10000L))
                    .build();
        }
    }

    private OrderSheet createOrderSheet() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderSheetItem sheetItem = OrderSheetItem.create("sheetItemId", product, price, itemCoupon, 1, options);
        return OrderSheet.create("sheetId", orderer, shippingAddress, List.of(sheetItem), cartCoupon, LocalDateTime.now(), 30);
    }
}
