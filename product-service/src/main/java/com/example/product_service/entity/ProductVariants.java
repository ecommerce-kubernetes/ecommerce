package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Setter
    private int price;
    @Setter
    private int stockQuantity;
    @Setter
    private int discountValue;

    public ProductVariants(String sku, int price, int stockQuantity, int discountValue){
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.discountValue = discountValue;
    }

    public void addProductVariantOptions(List<ProductVariantOptions> productVariantOptions){
        for (ProductVariantOptions productVariantOption : productVariantOptions) {
            addProductVariantOption(productVariantOption);
        }
    }

    public void addProductVariantOption(ProductVariantOptions productVariantOption){
        this.productVariantOptions.add(productVariantOption);
        productVariantOption.setProductVariant(this);
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

    public void addReview(Reviews review){
        reviews.add(review);
        review.setProductVariant(this);
    }

    public void deleteReview(Reviews reviews){
        this.reviews.remove(reviews);
        reviews.setProductVariant(null);
    }
}
