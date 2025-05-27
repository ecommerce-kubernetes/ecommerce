package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductVariants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Products product;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantOptions> productVariantOptions = new ArrayList<>();

    private String sku;
    private int price;
    private int stockQuantity;
    private int discountValue;

    public ProductVariants(Products product, String sku, int price, int stockQuantity, int discountValue){
        this.product = product;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.discountValue = discountValue;
    }

    public void addProductVariantOption(OptionValues optionValue){
        ProductVariantOptions productVariantOption = new ProductVariantOptions(this, optionValue);
        this.productVariantOptions.add(productVariantOption);
    }
}
