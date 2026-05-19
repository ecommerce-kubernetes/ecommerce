package com.example.order_service.ordersheet.application;

import com.example.order_service.common.domain.vo.Money;
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
    private OrderSheetProductGateway orderSheetProductGateway;
    @Mock
    private OrderSheetCouponGateway orderSheetCouponGateway;
    @Mock
    private OrderSheetUserGateway orderSheetUserGateway;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L);

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("쿠폰을 적용한 경우 쿠폰 정보를 조회하고 주문서를 생성한다")
        void createOrderSheet_coupon_applied(){
            //given
            OrderSheetCommand.Create command = createCouponAppliedCommand();
            List<OrderSheetProductResult.Info> products = createProducts();
            OrderSheetCouponResult.Calculate coupon = createCoupon();
            OrderSheetUserResult.Profile userProfile = createUserProfile();
            given(orderSheetUserGateway.getUserProfile(any())).willReturn(userProfile);
            given(orderSheetProductGateway.getProducts(anyList())).willReturn(products);
            given(orderSheetCouponGateway.calculate(any())).willReturn(coupon);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderSheetProductGateway).getProducts(anyList());
            verify(orderSheetCouponGateway).calculate(any());
        }

        @Test
        @DisplayName("쿠폰을 적용하지 않은 경우 쿠폰 정보를 조회하지 않고 주문서를 생성한다")
        void createOrderSheet_coupon_not_applied(){
            //given
            OrderSheetCommand.Create command = createNotCouponAppliedCommand();
            List<OrderSheetProductResult.Info> products = createProducts();
            OrderSheetUserResult.Profile userProfile = createUserProfile();
            given(orderSheetUserGateway.getUserProfile(any())).willReturn(userProfile);
            given(orderSheetProductGateway.getProducts(anyList())).willReturn(products);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderSheetProductGateway).getProducts(anyList());
            verify(orderSheetCouponGateway, never()).calculate(any());
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

        private OrderSheetUserResult.Profile createUserProfile() {
            OrderSheetUserResult.ShippingAddress shippingAddress = OrderSheetUserResult.ShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();
            return OrderSheetUserResult.Profile.builder()
                    .userId(1L)
                    .userName("주문자")
                    .phoneNumber("010-1234-5678")
                    .shippingAddress(shippingAddress)
                    .build();
        }

        private List<OrderSheetProductResult.Info> createProducts() {
            OrderSheetProductResult.Option size = OrderSheetProductResult.Option.builder()
                    .optionTypeName("사이즈")
                    .optionValueName("XL")
                    .build();
            OrderSheetProductResult.Option blue = OrderSheetProductResult.Option.builder()
                    .optionTypeName("색상")
                    .optionValueName("BLUE")
                    .build();
            OrderSheetProductResult.Info product = OrderSheetProductResult.Info.builder()
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
            return List.of(product);
        }

        private OrderSheetCouponResult.Calculate createCoupon() {
            OrderSheetCouponResult.CartCoupon cartCoupon = OrderSheetCouponResult.CartCoupon.builder()
                    .couponId(1L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            OrderSheetCouponResult.ItemCoupon itemCoupon = OrderSheetCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(2L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            return OrderSheetCouponResult.Calculate
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
            OrderSheetUserResult.UserPoint point = OrderSheetUserResult.UserPoint.builder()
                    .userId(1L)
                    .availablePoints(Money.wons(10000L))
                    .build();

            given(repository.findById(anyString())).willReturn(Optional.of(orderSheet));
            given(orderSheetUserGateway.getUserPoints(anyLong())).willReturn(point);
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
            OrderSheetUserResult.UserPoint point = OrderSheetUserResult.UserPoint.builder()
                    .userId(1L)
                    .availablePoints(Money.wons(10000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderSheetUserGateway.getUserPoints(any())).willReturn(point);
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

    private OrderSheet createOrderSheet() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderSheetItemProductSnapshot product = OrderSheetItemProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        OrderSheetItemPriceSnapshot price = OrderSheetItemPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<OrderSheetItemOptionSnapshot> options = List.of(
                OrderSheetItemOptionSnapshot.of("사이즈", "XL"),
                OrderSheetItemOptionSnapshot.of("색상", "BLUE")
        );
        OrderSheetItem sheetItem = OrderSheetItem.create("sheetItemId", product, price, itemCoupon, 1, options);
        return OrderSheet.create("sheetId", orderer, shippingAddress, List.of(sheetItem), cartCoupon, LocalDateTime.now(), 30);
    }
}
