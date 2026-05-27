package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.dto.result.OrderResult;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import com.example.order_service.order.application.util.OrderValidator;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderAppServiceTest {

    @InjectMocks
    private OrderAppService orderAppService;
    @Mock
    private OrderSheetRepository orderSheetRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderProductGateway orderProductGateway;
    @Mock
    private OrderCouponGateway orderCouponGateway;
    @Mock
    private OrderUserGateway orderUserGateway;
    @Spy
    private OrderValidator orderValidator = new OrderValidator();

    @Nested
    @DisplayName("주문 생성")
    class InitialOrder{

        @Test
        @DisplayName("주문을 생성한다")
        void initialOrder(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(1L)
                    .build();
            OrderUserResult.UserPoint userPoint = createUserResult();
            OrderProductResult.ProductList productResult = createProductResult();
            OrderCouponResult.Calculate couponResult = createCouponResult();
            OrderContext.CreateOrderContext orderContext = createOrderContext();
            OrderDto.Detail orderDto = createOrderDto();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPointsForOrder(anyLong(), any())).willReturn(userPoint);
            given(orderProductGateway.getProducts(anyList())).willReturn(productResult);
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderMapper.toContext(any())).willReturn(orderContext);
            given(orderService.saveOrder(any())).willReturn(orderDto);
            //when
            OrderResult.Create result = orderAppService.initialOrder(command);
            //then
            assertThat(result.orderNo()).isEqualTo("orderNo");
            assertThat(result.orderName()).isEqualTo("orderName");
        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void initialOrder_not_found_sheet(){
            //given
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(999L)
                    .build();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.empty());
            //when
            //then
            assertThatThrownBy(() -> orderAppService.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아닌 경우 예외가 발생한다")
        void initialOrder_no_permission(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(999L)
                    .build();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderAppService.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_SHEET_ACCESS_DENIED);
        }

        @Test
        @DisplayName("주문서가 만료된 경우 예외가 발생한다")
        void initialOrder_sheet_expired(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(1L)
                    .build();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderAppService.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
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

    private OrderUserResult.UserPoint createUserResult() {
        return OrderUserResult.UserPoint.builder()
                .userId(1L)
                .ownedPoints(Money.wons(10000L))
                .build();
    }

    private OrderProductResult.ProductList createProductResult() {
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        OrderProductResult.Info product = OrderProductResult.Info.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .options(List.of(xl, blue))
                .build();

        return OrderProductResult.ProductList.builder()
                .products(List.of(product))
                .build();
    }

    private OrderCouponResult.Calculate createCouponResult() {
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 할인 쿠폰", Money.wons(1000L));
        OrderCouponResult.ItemCoupon itemCouponResult = OrderCouponResult.ItemCoupon.builder()
                .productVariantId(1L)
                .itemCoupon(itemCoupon)
                .build();
        return OrderCouponResult.Calculate.builder()
                .cartCoupon(cartCoupon)
                .itemCoupons(List.of(itemCouponResult))
                .build();
    }

    private OrderContext.CreateOrderContext createOrderContext(){
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<OrderContext.ItemContext> orderItemContext = createOrderItemContext();
        return OrderContext.CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .orderItems(orderItemContext)
                .cartCoupon(cartCoupon)
                .totalOriginalPrice(Money.wons(10000L))
                .totalProductDiscountAmount(Money.wons(1000L))
                .totalCouponDiscountAmount(Money.wons(2000L))
                .usedPoints(Money.ZERO)
                .totalPaymentAmount(Money.wons(7000L))
                .build();
    }

    private List<OrderContext.ItemContext> createOrderItemContext() {
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderContext.ItemContext item = OrderContext.ItemContext.builder()
                .productSnapshot(product)
                .itemPrice(price)
                .itemCoupon(itemCoupon)
                .quantity(1)
                .options(options).build();
        return List.of(item);
    }

    private OrderDto.Detail createOrderDto() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        List<OrderDto.Item> orderItem = createOrderItem();
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        return OrderDto.Detail.builder()
                .id(1L)
                .orderNo("orderNo")
                .orderName("orderName")
                .status(OrderStatus.PENDING)
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .orderItems(orderItem)
                .cartCoupon(cartCoupon)
                .totalOriginalPrice(Money.wons(10000L))
                .totalProductDiscountAmount(Money.wons(1000L))
                .totalCouponDiscountAmount(Money.wons(2000L))
                .usedPoints(Money.ZERO)
                .totalPaymentAmount(Money.wons(7000L))
                .build();
    }

    private List<OrderDto.Item> createOrderItem() {
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderDto.Item item = OrderDto.Item.builder()
                .product(product)
                .productPrice(price)
                .itemCoupon(itemCoupon)
                .quantity(1)
                .options(options)
                .build();
        return List.of(item);
    }
}
