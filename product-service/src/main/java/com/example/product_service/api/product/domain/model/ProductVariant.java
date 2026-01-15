package com.example.product_service.api.product.domain.model;

import com.example.product_service.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantOption> productVariantOptions = new ArrayList<>();

    private String sku;
    private Long price;
    private Long originalPrice;
    private Integer stockQuantity;
    private Integer discountRate;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductVariant(String sku, Long price, Long originalPrice, Integer stockQuantity, Integer discountRate) {
        this.sku = sku;
        this.price = price;
        this.originalPrice = originalPrice;
        this.stockQuantity = stockQuantity;
        this.discountRate = discountRate;
    }

    public static ProductVariant create(String sku, Long originalPrice, Integer stockQuantity, Integer discountRate) {
        Long price = calculatePrice(originalPrice, discountRate);
        return ProductVariant.builder()
                .sku(sku)
                .originalPrice(originalPrice)
                .price(price)
                .stockQuantity(stockQuantity)
                .discountRate(discountRate)
                .build();
    }

    private static Long calculatePrice(Long originalPrice, Integer discountRate) {
        if (discountRate == null || discountRate == 0 ){
            return originalPrice;
        }
        return originalPrice * (100 - discountRate)/100;
    }

    protected void setProduct(Product product) {
        this.product = product;
    }

    public void reductionStock(int stock){
        if(stockQuantity - stock < 0){
            throw new InsufficientStockException("Out of Stock");
        }
        this.stockQuantity = this.stockQuantity - stock;
    }

    public void restoreStock(int stock){
        this.stockQuantity = this.stockQuantity + stock;
    }
}
