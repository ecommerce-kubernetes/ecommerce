package com.example.order_service.order.domain.order;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.ProductOptionSnapshotConverter;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItem extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Embedded
    private ProductSnapshot product;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "discountAmount", column = @Column(name = "product_discount_amount"))
    })
    private ProductPriceSnapshot productPrice;

    @Embedded
    private AppliedItemCoupon appliedItemCoupon;

    private Integer quantity;

    @Convert(converter = ProductOptionSnapshotConverter.class)
    private List<ProductOptionSnapshot> options;

    @Embedded
    private OrderItemAmount orderItemAmount;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(Long id, ProductSnapshot product, ProductPriceSnapshot productPrice, AppliedItemCoupon appliedItemCoupon,
                      Integer quantity, List<ProductOptionSnapshot> options, OrderItemAmount orderItemAmount) {
        Assert.notNull(id, "주문 항목(OrderItem) 생성시 아이디는 필수이다.");
        Assert.notNull(product, "주문 항목(OrderItem) 생성시 상품 정보는 필수이다.");
        Assert.notNull(productPrice, "주문 항목(OrderItem) 생성시 상품 가격 정보는 필수이다.");
        Assert.notNull(quantity, "주문 항목(OrderItem) 생성시 주문 수량은 필수이다.");
        Assert.notNull(options, "주문 항목(OrderItem) 생성시 상품 옵션은 필수이다.");
        Assert.notNull(orderItemAmount, "주문 항목(OrderItem) 생성시 주문 항목 가격 정보는 필수이다.");

        this.id = id;
        this.product = product;
        this.productPrice = productPrice;
        this.appliedItemCoupon = appliedItemCoupon;
        this.quantity = quantity;
        this.options = options;
        this.orderItemAmount = orderItemAmount;
    }

    public static OrderItem create(CreateOrderItemContext context, IdGenerator idGenerator) {
        if (context.quantity() <= 0) {
            throw new BusinessException(OrderErrorCode.INVALID_ITEM_QUANTITY);
        }

        Long id = idGenerator.generate();

        return OrderItem.builder()
                .id(id)
                .product(context.productSnapshot())
                .productPrice(context.priceSnapshot())
                .appliedItemCoupon(context.appliedItemCoupon())
                .quantity(context.quantity())
                .options(context.options())
                .orderItemAmount(context.orderItemAmount())
                .build();
    }
}

