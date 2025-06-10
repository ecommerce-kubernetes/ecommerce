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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reviews> reviews = new ArrayList<>();

    private String sku; //TS-M-BLUE
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

    public int getDiscountPrice(){
        int originPrice = this.price;
        return Math.round(originPrice * (100 - discountValue)/ 100f);
    }

    protected void setProduct(Products product){
        this.product = product;
    }

    public void addReview(Long userId, String userName,  int rating, String content, List<String> imageUrls){
        Reviews review = new Reviews(this, userId, userName ,rating, content);
        reviews.add(review);
        for (String imageUrl : imageUrls) {
            review.addImage(imageUrl);
        }
    }
}
