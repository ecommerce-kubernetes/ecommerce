package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderSheetFactoryTest {

    private final OrderSheetFactory orderSheetFactory = new OrderSheetFactory();

    @Test
    @DisplayName("주문서 도메인을 생성한다")
    void createSheet(){
        //given
        OrderSheetCommand.Create command = createCommand();
        OrderUserResult.Profile userProfile = createUserProfile();
        OrderProductResult productList = createProductList();
        OrderCouponResult.Calculate couponResult = createCouponResult();
        LocalDateTime currentTime = LocalDateTime.now();
        //when
        OrderSheet result = orderSheetFactory.createSheet(command, userProfile, productList, couponResult, 30);
        //then
        assertThat(result.getOrderer()).isEqualTo(userProfile.orderer());
        assertThat(result.getShippingAddress()).isEqualTo(userProfile.shippingAddress());
        assertThat(result.getItems()).hasSize(command.items().size());
        assertThat(result.getCartCoupon()).isEqualTo(couponResult.cartCoupon());
        assertThat(result.getUsedPoints()).isEqualTo(Money.ZERO);
        assertThat(result.isExpired(currentTime)).isFalse();
    }

    //TODO 빈 쿠폰 매핑 테스트 작성

    private OrderSheetCommand.Create createCommand() {
        OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        OrderSheetCommand.ItemCoupon itemCoupon = OrderSheetCommand.ItemCoupon.builder()
                .productVariantId(1L)
                .couponId(2L)
                .build();
        return OrderSheetCommand.Create.builder()
                .userId(1L)
                .cartCouponId(1L)
                .items(List.of(item))
                .itemCoupons(List.of(itemCoupon))
                .build();
    }

    private OrderUserResult.Profile createUserProfile(){
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        return OrderUserResult.Profile
                .builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .build();
    }

    private OrderProductResult createProductList() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE",
                "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L),
                10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
        OrderProductResult.OrderProductDetail item = OrderProductResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(productPriceSnapshot)
                .options(List.of(xl, blue))
                .build();
        return OrderProductResult.builder()
                .products(List.of(item))
                .build();
    }

    private OrderCouponResult.Calculate createCouponResult() {
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", Money.wons(1000L));
//        ItemCouponSnapshot itemCouponInfo = ItemCouponSnapshot.of(2L, "하의 1000원 할인 쿠폰", Money.wons(1000L));
        OrderCouponResult.ItemCoupon itemCoupon = OrderCouponResult.ItemCoupon.builder()
                .productVariantId(1L)
//                .itemCoupon(itemCouponInfo)
                .build();
        return OrderCouponResult.Calculate.builder()
//                .cartCoupon(cartCoupon)
                .itemCoupons(List.of(itemCoupon))
                .build();
    }
}
