package com.example.order_service.order.adapter.out.client.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.adapter.out.persistence.entity.OrderSheetRedisEntity;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSheetRedisMapperTest {
    private final PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
    private final OrderSheetRedisMapper mapper = new OrderSheetRedisMapper();
    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문서 도메인을 엔티티로 변환한다")
    void toEntityAndToDomain() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        //when
        OrderSheetRedisEntity entity = mapper.toEntity(orderSheet);
        OrderSheet restoredDomain = mapper.toDomain(entity);
        //then
        assertThat(restoredDomain)
                .usingRecursiveComparison()
                .isEqualTo(orderSheet);
    }

    private OrderSheet createOrderSheet() {
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

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

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

        OrderSheet orderSheet = OrderSheet.create(context, idGenerator);

        OrderSheetItem item = orderSheet.getItems().getFirst();

        orderSheet.changeShippingAddress(shippingAddress);
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointUsagePolicy);
        orderSheet.applyCartCoupon(cartCoupon, pointUsagePolicy);

        orderSheet.applyPoints(Money.wons(1000L), pointUsagePolicy);
        return orderSheet;
    }
}