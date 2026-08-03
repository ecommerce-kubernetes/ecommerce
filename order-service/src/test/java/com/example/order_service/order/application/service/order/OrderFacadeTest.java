package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.OrderUserPort;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetItem;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;
    @Mock
    private OrderSheetRepository orderSheetRepository;
    @Mock
    private OrderCommandService orderCommandService;
    @Mock
    private OrderProductPort orderProductPort;
    @Mock
    private OrderCouponPort orderCouponPort;
    @Mock
    private OrderUserPort orderUserPort;
    @Spy
    private PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
    @Spy
    private OrderValidator orderValidator;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
    
    
    @Test
    @DisplayName("주문을 생성한다.")
    void createOrder() {
        //given
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderSheetId(1L)
                .userId(1L)
                .build();
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(30);
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        OrderProductsResult.OrderProductDetail product = createProductDetail(1L, OrderProductStatus.ON_SALE, 100);
        OrderProductsResult products = OrderProductsResult.builder()
                .products(List.of(product))
                .build();

        ItemCouponsResult.ItemCouponResult itemCouponResult = createItemCouponResult();
        ItemCouponsResult itemCoupons = ItemCouponsResult.builder()
                .userId(1L)
                .itemCoupons(List.of(itemCouponResult))
                .build();

        CartCouponResult cartCouponResult = createCartCouponResult();
        OrdererProfileResult ordererProfile = createOrdererProfile();

        given(orderSheetRepository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderProductPort.getProducts(anyList())).willReturn(products);
        given(orderCouponPort.getItemCoupons(anyLong(), anyList())).willReturn(itemCoupons);
        given(orderCouponPort.getCartCoupon(anyLong(), anyLong())).willReturn(cartCouponResult);
        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfile);
        given(orderCommandService.saveOrder(any(CreateOrderContext.class))).willReturn(1L);
        //when
        OrderCreateResult result = orderFacade.createOrder(command);
        //then
        assertThat(result.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다.")
    void createOrder_notFound_orderSheet() {
        //given
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderSheetId(1L)
                .userId(1L)
                .build();
        given(orderSheetRepository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("주문서가 만료되었으면 예외가 발생한다.")
    void createOrder_expired_orderSheet() {
        //given
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderSheetId(1L)
                .userId(1L)
                .build();
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(20);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        given(orderSheetRepository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    private OrderSheet createOrderSheet(LocalDateTime expiresAt) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "청바지 1000원 할인", couponDiscountPolicy, 1);
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "장바구니 1000원 할인", couponDiscountPolicy, Money.wons(10000L));

        CreateOrderSheetItemContext itemCtx = CreateOrderSheetItemContext.builder()
                .productSnapshot(product)
                .priceSnapshot(price)
                .quantity(5)
                .optionSnapshots(options)
                .build();

        CreateOrderSheetContext context = CreateOrderSheetContext.builder()
                .orderer(orderer)
                .items(List.of(itemCtx))
                .expiresAt(expiresAt)
                .build();

        IdGenerator idGenerator = new TsidGenerator();

        OrderSheet orderSheet = OrderSheet.create(context, idGenerator);

        OrderSheetItem item = orderSheet.getItems().getFirst();

        orderSheet.changeShippingAddress(shippingAddress);
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointUsagePolicy);
        orderSheet.applyCartCoupon(cartCoupon, pointUsagePolicy);

        orderSheet.applyPoints(Money.wons(1000L), pointUsagePolicy);
        return orderSheet;
    }

    private OrderProductsResult.OrderProductDetail createProductDetail(Long variantId, OrderProductStatus status, int stock) {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, variantId, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot option1 = ProductOptionSnapshot.of("옵션1", "옵션 값");
        ProductOptionSnapshot option2 = ProductOptionSnapshot.of("옵션2", "옵션 값");
        return OrderProductsResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(status)
                .stock(stock)
                .priceSnapshot(priceSnapshot)
                .options(List.of(option1, option2))
                .build();
    }

    private ItemCouponsResult.ItemCouponResult createItemCouponResult() {
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "청바지 1000원 할인", couponDiscountPolicy, 1);
        return ItemCouponsResult.ItemCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .itemCoupon(itemCoupon)
                .expiresAt(LocalDateTime.now(clock).plusDays(1))
                .build();
    }

    private CartCouponResult createCartCouponResult() {
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "장바구니 1000원 할인", couponDiscountPolicy, Money.wons(10000L));
        return CartCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .cartCoupon(cartCoupon)
                .expiresAt(LocalDateTime.now(clock).plusDays(1))
                .build();
    }

    private OrdererProfileResult createOrdererProfile() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");

        return OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(Money.wons(10000L))
                .defaultShippingAddress(shippingAddress)
                .build();
    }

}
