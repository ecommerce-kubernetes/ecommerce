package com.example.order_service.order.domain.model;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.util.ProductOptionSnapshotConverter;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @AttributeOverrides({
            @AttributeOverride(name = "discountAmount", column = @Column(name = "coupon_discount_amount"))
    })
    private OrderCouponSnapshot itemCoupon;
    private Integer quantity;
    @Convert(converter = ProductOptionSnapshotConverter.class)
    private List<ProductOptionSnapshot> options;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(ProductSnapshot product, ProductPriceSnapshot productPrice, OrderCouponSnapshot itemCoupon,
                      Integer quantity, List<ProductOptionSnapshot> options) {
        this.product = product;
        this.productPrice = productPrice;
        this.itemCoupon = itemCoupon;
        this.quantity = quantity;
        this.options = options;
    }

    public static OrderItem create(ProductSnapshot product, ProductPriceSnapshot productPrice, Integer quantity, List<ProductOptionSnapshot> options) {
        return OrderItem.builder()
                .product(product)
                .productPrice(productPrice)
                .quantity(quantity)
                .options(options)
                .build();
    }

    protected void setOrder(Order order) {
        this.order = order;
    }
}

