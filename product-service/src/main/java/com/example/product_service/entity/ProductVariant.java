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
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantOption> productVariantOptions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    private String sku; //TS-M-BLUE
    @Setter
    private int price;
    @Setter
    private int stockQuantity;
    @Setter
    private int discountValue;

    public ProductVariant(String sku, int price, int stockQuantity, int discountValue){
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.discountValue = discountValue;
    }

    public void addProductVariantOptions(List<ProductVariantOption> productVariantOptions){
        for (ProductVariantOption productVariantOption : productVariantOptions) {
            addProductVariantOption(productVariantOption);
        }
    }

    public void addProductVariantOption(ProductVariantOption productVariantOption){
        this.productVariantOptions.add(productVariantOption);
        productVariantOption.setProductVariant(this);
    }

    public int getDiscountPrice(){
        int originPrice = this.price;
        return Math.round(originPrice * (100 - discountValue)/ 100f);
    }

    protected void setProduct(Product product){
        this.product = product;
    }

    public void addReview(Long userId, String userName,  int rating, String content, List<String> imageUrls){
        Review review = new Review(this, userId, userName ,rating, content);
        reviews.add(review);
        for (String imageUrl : imageUrls) {
            review.addImage(imageUrl);
        }
    }

    public void addReview(Review review){
        reviews.add(review);
        review.setProductVariant(this);
    }

    public void deleteReview(Review review){
        this.reviews.remove(review);
        review.setProductVariant(null);
    }
}
